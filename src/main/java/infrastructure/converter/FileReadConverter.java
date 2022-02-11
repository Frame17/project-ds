package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.FileReadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileReadConverter implements PayloadConverter<FileReadMessage> {

    public FileReadMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            int fileNameLength = buffer.getInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            buffer.get(fileNameBytes);
            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

            byte[] file = null;
            if (buffer.hasRemaining() && buffer.get() == 1) {
                int fileLength = buffer.getInt();
                file = new byte[fileLength];
                buffer.get(file);
            }

            RemoteNode client = null;
            if (buffer.hasRemaining()) {
                byte[] ip = new byte[4 * Byte.BYTES];
                buffer.get(ip);
                client = new RemoteNode(InetAddress.getByAddress(ip), buffer.getInt());
            }

            return new FileReadMessage(fileName, file, client);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, FileReadMessage message) {
        byte[] fileName = message.fileName().getBytes(StandardCharsets.UTF_8);
        int bufferLength = Byte.BYTES + 1;
        if (message.file() == null && message.client() == null) {
            bufferLength += Integer.BYTES + fileName.length;
        } else if (message.client() == null) {
            bufferLength += Integer.BYTES + fileName.length + Integer.BYTES + message.file().length;
        } else if (message.file() == null) {
            bufferLength += Integer.BYTES + fileName.length + 4 * Byte.BYTES + Integer.BYTES;
        } else {
            bufferLength += Integer.BYTES + fileName.length + Integer.BYTES + message.file().length + 4 * Byte.BYTES + Integer.BYTES;
        }

        ByteBuffer messageBuffer = ByteBuffer.allocate(bufferLength);
        messageBuffer.put(command.command);
        messageBuffer.putInt(fileName.length);
        messageBuffer.put(fileName);
        if (message.file() != null) {
            messageBuffer.put((byte) 1);
            messageBuffer.putInt(message.file().length);
            messageBuffer.put(message.file());
        } else {
            messageBuffer.put((byte) -1);
        }
        if (message.client() != null) {
            messageBuffer.put(message.client().ip().getAddress());
            messageBuffer.putInt(message.client().port());
        }

        return messageBuffer.array();
    }
}
