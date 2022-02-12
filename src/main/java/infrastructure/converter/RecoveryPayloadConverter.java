package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.HealthMessage;
import infrastructure.system.message.RecoveryMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RecoveryPayloadConverter implements PayloadConverter<RecoveryMessage> {

    @Override
    public RecoveryMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        byte[] address = new byte[4 * Byte.BYTES];
        buffer.get(address);
        int port = buffer.getInt();

        try {
            RemoteNode node = new RemoteNode(InetAddress.getByAddress(address), port);
            ArrayList<String> fileChunks = new ArrayList<>();

            if (buffer.hasRemaining()) {
                byte fileChunksCount = buffer.get();
                for (int i = 0; i < fileChunksCount; i++) {
                    int chunkNameLength = buffer.getInt();
                    byte[] chunkNameBytes = new byte[chunkNameLength];
                    buffer.get(chunkNameBytes);
                    fileChunks.add(new String(chunkNameBytes, StandardCharsets.UTF_8));
                }
            }

            return new RecoveryMessage(node, fileChunks);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, RecoveryMessage message) {
        int bufferSize;
        if (message.fileChunks() == null) {
            bufferSize = Byte.BYTES + 4 * Byte.BYTES + Integer.BYTES;
        } else {
            int fileChunksCount = message.fileChunks().size();
            Integer fileChunksSize = message.fileChunks().stream()
                    .map(it -> it.getBytes(StandardCharsets.UTF_8).length)
                    .reduce(0, Integer::sum);

            bufferSize = Byte.BYTES + 4 * Byte.BYTES + Integer.BYTES + Byte.BYTES + 4 * fileChunksCount + fileChunksSize;
        }

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.put(command.command);
        buffer.put(message.node().ip().getAddress());
        buffer.putInt(message.node().port());

        if (message.fileChunks() != null) {
            buffer.put((byte) message.fileChunks().size());
            message.fileChunks().forEach(
                    fileChunk -> {
                        byte[] chunkBytes = fileChunk.getBytes(StandardCharsets.UTF_8);
                        buffer.putInt(chunkBytes.length);
                        buffer.put(chunkBytes);
                    }
            );
        }

        return buffer.array();
    }
}
