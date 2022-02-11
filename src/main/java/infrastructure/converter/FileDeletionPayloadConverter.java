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
        return new FileDeletionMessage(fileNameLength, fileName);
    }

    @Override
    public byte[] encode(Command command, FileDeletionMessage message) {
        ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + message.Filename().length());
        messageBuffer.put(command.command);
        messageBuffer.putInt(message.FileNameLength());
        byte[] fileNameBytes = message.Filename().getBytes(StandardCharsets.UTF_8);
        messageBuffer.put(fileNameBytes);
        return new byte[0];
    }
}
