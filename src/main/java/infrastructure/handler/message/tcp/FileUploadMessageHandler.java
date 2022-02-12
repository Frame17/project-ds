package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.handler.message.udp.UdpMessageHandler;
import infrastructure.system.FileChunk;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileUploadMessage;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

public class FileUploadMessageHandler implements UdpMessageHandler {
    public static final int REPLICATION_NUMBER = 1;
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<FileUploadMessage> converter;

    public FileUploadMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<FileUploadMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        FileUploadMessage fileUploadMessage = converter.decode(packet.getData());

        if (context.isLeader()) {
            List<RemoteNode> aliveNodes = context.getLeaderContext().aliveNodes.keySet().stream().toList();

            ByteBuffer buffer = ByteBuffer.wrap(fileUploadMessage.file());
            int chunkSize = (int) ceil((double) fileUploadMessage.file().length / (1 + context.getLeaderContext().aliveNodes.size()));
            byte[] chunk = new byte[chunkSize];
            buffer.get(chunk);
            saveFile(fileUploadMessage.fileName() + "-0", chunk);

            ArrayList<FileChunk> fileChunks = new ArrayList<>();
            fileChunks.add(new FileChunk(fileUploadMessage.fileName() + "-0", new RemoteNode(context.getLeader().ip(), context.getLeader().port())));
            for (int i = 0; i < REPLICATION_NUMBER && i < aliveNodes.size(); i++) {
                RemoteNode target = aliveNodes.get(i);
                try {
                    client.unicast(converter.encode(Command.FILE_UPLOAD, new FileUploadMessage(fileUploadMessage.fileName() + "-0", chunk)),
                            target.ip(), target.port() + 1);
                    fileChunks.add(new FileChunk(fileUploadMessage.fileName() + "-0", target));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            for (int i = 0; i < aliveNodes.size(); i++) {
                chunk = new byte[Math.min(chunkSize, buffer.remaining())];
                buffer.get(chunk);

                try {
                    RemoteNode target = aliveNodes.get(i);
                    client.unicast(converter.encode(Command.FILE_UPLOAD, new FileUploadMessage(fileUploadMessage.fileName() + '-' + (i + 1), chunk)),
                            target.ip(), target.port() + 1);
                    fileChunks.add(new FileChunk(fileUploadMessage.fileName() + '-' + (i + 1), target));
                    for (int j = 0; j < REPLICATION_NUMBER && j < aliveNodes.size(); j++) {
                        target = aliveNodes.get((j + i + 1) % aliveNodes.size());
                        client.unicast(converter.encode(Command.FILE_UPLOAD, new FileUploadMessage(fileUploadMessage.fileName() + '-' + (i + 1), chunk)),
                                target.ip(), target.port() + 1);
                        fileChunks.add(new FileChunk(fileUploadMessage.fileName() + '-' + (i + 1), target));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            context.getLeaderContext().chunksDistributionTable.put(fileUploadMessage.fileName(), fileChunks);
        } else {
            saveFile(fileUploadMessage.fileName(), fileUploadMessage.file());
        }
    }

    private void saveFile(String fileName, byte[] content) {
        try {
            Files.write(new File(fileName).toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
