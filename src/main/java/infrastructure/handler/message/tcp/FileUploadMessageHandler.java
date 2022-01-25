package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

public class FileUploadMessageHandler implements TcpMessageHandler {
    private final RemoteClient<byte[]> client;

    public FileUploadMessageHandler(RemoteClient<byte[]> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        if (context.isLeader()) {
            List<RemoteNode> aliveNodes = context.getLeaderContext().aliveNodes.keySet().stream().toList();
            HashMap<RemoteNode, String> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;

            int chunkSize = (int) ceil((double) buffer.remaining() / (1 + context.getLeaderContext().aliveNodes.size()));
            byte[] chunk = new byte[chunkSize];
            buffer.get(chunk);
            saveFile(fileName + "-0", chunk);
            chunksDistributionTable.put(new RemoteNode(context.getLeader().leaderIp(), context.getLeader().leaderPort()), fileName + "-0");

            for (int i = 0; i < aliveNodes.size(); i++) {
                byte[] chunkName = (fileName + '-' + (i + 1)).getBytes(StandardCharsets.UTF_8);
                ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + chunkName.length + min(chunkSize, buffer.remaining()));
                messageBuffer.put(Command.FILE_UPLOAD.command);
                messageBuffer.putInt(chunkName.length);
                messageBuffer.put(chunkName);
                chunk = new byte[chunkSize];
                buffer.get(chunk);
                messageBuffer.put(chunk);

                try {
                    RemoteNode target = aliveNodes.get(i);
                    client.unicast(messageBuffer.array(), target.ip(), target.port());
                    chunksDistributionTable.put(new RemoteNode(target.ip(), target.port()), fileName + '-' + (i + 1));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            byte[] chunk = new byte[buffer.remaining()];
            buffer.get(chunk);
            saveFile(fileName, chunk);
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
