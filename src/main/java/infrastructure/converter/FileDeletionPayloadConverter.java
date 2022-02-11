package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.FileDeletionMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileDeletionPayloadConverter implements PayloadConverter<FileDeletionMessage> {
    @Override
    public FileDeletionMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        return new FileDeletionMessage(fileName);
    }

    @Override
    public byte[] encode(Command command, FileDeletionMessage message) {
        byte[] fileNameBytes = message.filename().getBytes(StandardCharsets.UTF_8);
        ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileNameBytes.length);
        messageBuffer.put(command.command);
        messageBuffer.putInt(fileNameBytes.length);
        messageBuffer.put(fileNameBytes);

        return messageBuffer.array();
    }
}
