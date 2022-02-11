package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.FileReadDataMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileReadDataPayloadConverter implements PayloadConverter<FileReadDataMessage>{
    @Override
    public FileReadDataMessage decode(byte[] payload) {
        try {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        int fileNameLength = buffer.getInt();

        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);

        byte[] ipBytes = new byte[4 * Byte.BYTES];
        buffer.get(ipBytes);

        int port = buffer.getInt();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
        InetAddress ip = InetAddress.getByAddress(ipBytes);

        return new FileReadDataMessage(fileName, data, ip, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, FileReadDataMessage message) {

        byte[] fileNameBytes = message.fileName().getBytes(StandardCharsets.UTF_8);
        byte[] ipBytes = message.ip().getAddress();

        ByteBuffer fileReadDataBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileNameBytes.length + ipBytes.length + Integer.BYTES + message.data().length );

        fileReadDataBuffer.put(command.command);
        fileReadDataBuffer.putInt(fileNameBytes.length);
        fileReadDataBuffer.put(fileNameBytes);
        fileReadDataBuffer.put(ipBytes);
        fileReadDataBuffer.putInt(message.port());
        fileReadDataBuffer.put(message.data());

        return fileReadDataBuffer.array();
    }
}
