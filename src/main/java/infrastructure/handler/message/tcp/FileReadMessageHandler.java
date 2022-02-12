package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.handler.message.udp.UdpMessageHandler;
import infrastructure.system.*;
import infrastructure.system.message.FileReadMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

public class FileReadMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(FileReadMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<FileReadMessage> converter;

    public FileReadMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<FileReadMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        FileReadMessage fileReadMessage = converter.decode(packet.getData());
        LOG.info("File-Read-Request {}", fileReadMessage.fileName());

        if (context.isLeader()) {
            if (fileReadMessage.file() == null) {  // user request
                HashMap<String, byte[]> chunks = new HashMap<>();
                context.getLeaderContext().chunksDistributionTable.get(fileReadMessage.fileName())
                        .forEach(chunk -> chunks.put(chunk.name(), new byte[0]));

                if (context.getLeaderContext().fileReadRequests.containsKey(fileReadMessage.fileName())) {
                    context.getLeaderContext().fileReadRequests.get(fileReadMessage.fileName()).second().add(fileReadMessage.client());
                } else {
                    ArrayList<RemoteNode> clients = new ArrayList<>();
                    clients.add(fileReadMessage.client());
                    context.getLeaderContext().fileReadRequests.put(fileReadMessage.fileName(), new Pair<>(new FileReadRequest(fileReadMessage.fileName(), chunks), clients));
                }

                FileReadRequest fileReadRequest = context.getLeaderContext().fileReadRequests.get(fileReadMessage.fileName()).first();
                requestFileChunks(context, fileReadMessage, fileReadRequest);
            } else {
                String fileName = fileReadMessage.fileName().substring(0, fileReadMessage.fileName().lastIndexOf('-'));
                if (context.getLeaderContext().fileReadRequests.containsKey(fileName)) {
                    FileReadRequest fileReadRequest = context.getLeaderContext().fileReadRequests
                            .get(fileName)
                            .first();
                    fileReadRequest.chunks().put(fileReadMessage.fileName(), fileReadMessage.file());

                    if (fileReadRequest.isComplete()) {
                        sendReadReplies(context, fileReadMessage);
                        context.getLeaderContext().fileReadRequests.remove(fileReadRequest.fileName());
                    }
                }
            }
        } else {
            try {
                client.unicast(converter.encode(Command.FILE_READ, new FileReadMessage(fileReadMessage.fileName(), readFile(fileReadMessage.fileName()), null)),
                        context.getLeader().ip(), context.getLeader().port() + 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void requestFileChunks(SystemContext context, FileReadMessage fileReadMessage, FileReadRequest fileReadRequest) {
        context.getLeaderContext().chunksDistributionTable.get(fileReadMessage.fileName())
                .forEach(chunk -> {
                    try {
                        if (IdService.nodeId(chunk.node().ip(), chunk.node().port()).equals(context.id)) {
                            fileReadRequest.chunks().put(chunk.name(), readFile(chunk.name()));
                        } else {
                            FileReadMessage fileReadMessageSend = new FileReadMessage(chunk.name(), null, null);
                            this.client.unicast(converter.encode(Command.FILE_READ, fileReadMessageSend), chunk.node().ip(), chunk.node().port() + 1);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void sendReadReplies(SystemContext context, FileReadMessage fileReadMessage) {
        Pair<FileReadRequest, List<RemoteNode>> requestClients = context.getLeaderContext().fileReadRequests
                .get(fileReadMessage.fileName().substring(0, fileReadMessage.fileName().lastIndexOf('-')));
        requestClients.second()
                .forEach(clientNode -> {
                    try {
                        client.unicast(converter.encode(Command.FILE_READ, new FileReadMessage(requestClients.first().fileName(), assembleFile(requestClients), null)),
                                clientNode.ip(), clientNode.port());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private byte[] assembleFile(Pair<FileReadRequest, List<RemoteNode>> requestClients) {
        int fileSize = requestClients.first().chunks()
                .values().stream()
                .reduce(0, (acc, bytes) -> acc + bytes.length, Integer::sum);

        ByteBuffer fileBuffer = ByteBuffer.allocate(fileSize);
        requestClients.first().chunks().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(chunk -> fileBuffer.put(chunk.getValue()));

        return fileBuffer.array();
    }

    private byte[] readFile(String fileName) throws IOException {
        return Files.readAllBytes(new File("files/" + fileName).toPath());
    }
}