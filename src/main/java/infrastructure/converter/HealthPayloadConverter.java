package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.HealthMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class HealthPayloadConverter implements PayloadConverter<HealthMessage> {

    @Override
    public HealthMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        byte[] address = new byte[4 * Byte.BYTES];
        buffer.get(address);

        try {
            return new HealthMessage(new RemoteNode(InetAddress.getByAddress(address) ,buffer.getInt()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, HealthMessage message) {
        byte[] address = message.node().ip().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length + Integer.BYTES);
        buffer.put(Command.HEALTH.command);
        buffer.put(address);
        buffer.putInt(message.node().port());

        return buffer.array();
    }
}
