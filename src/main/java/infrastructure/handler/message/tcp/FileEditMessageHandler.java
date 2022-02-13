package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.handler.message.udp.UdpMessageHandler;
import infrastructure.system.IdService;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileEditMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.ceil;

public class FileEditMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(FileEditMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<FileEditMessage> converter;

    public FileEditMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<FileEditMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        FileEditMessage fileEditMessage = converter.decode(packet.getData());
        LOG.info("File-Edit-Request {}", fileEditMessage.fileName());

        if (context.isLeader()) {
            ByteBuffer buffer = ByteBuffer.wrap(fileEditMessage.file());
            int chunkSize = (int) ceil((double) fileEditMessage.file().length / (1 + context.getLeaderContext().aliveNodes.size()));
            Map<String, byte[]> fileChunks = new HashMap<>();
            for (int i = 0; i < context.getLeaderContext().aliveNodes.size() + 1; i++) {
                byte[] chunk = new byte[Math.min(chunkSize, buffer.remaining())];
                buffer.get(chunk);
                fileChunks.put(fileEditMessage.fileName() + '-' + i, chunk);
            }

            context.getLeaderContext().chunksDistributionTable.get(fileEditMessage.fileName())
                    .forEach(fileChunk -> {
                        if (context.id.equals(IdService.nodeId(fileChunk.node().ip(), fileChunk.node().port()))) {
                            replaceFile(fileChunk.name(), fileChunks.get(fileChunk.name()));
                        } else {
                            try {
                                client.unicast(converter.encode(Command.FILE_EDIT, new FileEditMessage(fileChunk.name(), fileChunks.get(fileChunk.name()))),
                                        fileChunk.node().ip(), fileChunk.node().port() + 1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } else {
            replaceFile(fileEditMessage.fileName(), fileEditMessage.file());
        }
    }

    private void replaceFile(String fileName, byte[] content) {
        try {
            Path path = new File("files/" + fileName).toPath();
            Files.delete(path);
            Files.write(path, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
