package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.FileEditMessage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileEditConverter implements PayloadConverter<FileEditMessage> {

    @Override
    public FileEditMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);

        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        int fileLength = buffer.getInt();
        byte[] file = new byte[fileLength];
        buffer.get(file);

        return new FileEditMessage(new String(fileNameBytes, StandardCharsets.UTF_8), file);
    }

    @Override
    public byte[] encode(Command command, FileEditMessage message) {
        byte[] fileName = message.fileName().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileName.length + Integer.BYTES + message.file().length);
        buffer.put(command.command);
        buffer.putInt(fileName.length);
        buffer.put(fileName);
        buffer.putInt(message.file().length);
        buffer.put(message.file());

        return buffer.array();
    }
}
