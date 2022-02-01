package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.StartMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class StartPayloadConverter implements PayloadConverter<StartMessage> {

    @Override
    public StartMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        byte[] ip = new byte[4 * Byte.BYTES];
        buffer.get(ip);
        try {
            return new StartMessage(InetAddress.getByAddress(ip), buffer.getInt());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, StartMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + 4 * Byte.BYTES + Integer.BYTES);

        buffer.put(command.command);
        buffer.put(message.ip().getAddress());
        buffer.putInt(message.port());

        return buffer.array();
    }
}
