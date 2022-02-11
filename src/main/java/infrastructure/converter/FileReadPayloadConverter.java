package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.FileReadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileReadPayloadConverter implements PayloadConverter<FileReadMessage> {
    @Override
    public FileReadMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            int fileNameLength = buffer.getInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            buffer.get(fileNameBytes);

            byte[] ipBytes = new byte[4 * Byte.BYTES];
            buffer.get(ipBytes);
            int port = buffer.getInt();

            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
            InetAddress ip = InetAddress.getByAddress(ipBytes);

            return new FileReadMessage(fileName, ip, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, FileReadMessage message) {

        byte[] fileName = message.fileName().getBytes(StandardCharsets.UTF_8);
        byte[] ip = message.ip().getAddress();

        ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileName.length + ip.length + Integer.BYTES);

        messageBuffer.put(command.command);
        messageBuffer.putInt(fileName.length);
        messageBuffer.put(fileName);
        messageBuffer.put(ip);
        messageBuffer.putInt(message.port());

        return messageBuffer.array();
    }
}
