package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.ResendMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ResendPayloadConverter implements PayloadConverter<ResendMessage> {

    @Override
    public ResendMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        byte[] address = new byte[4 * Byte.BYTES];
        buffer.get(address);
        int port = buffer.getInt();

        try {
            return new ResendMessage(new RemoteNode(InetAddress.getByAddress(address), port), buffer.getInt());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, ResendMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + 4 * Byte.BYTES + Integer.BYTES + Integer.BYTES);
        buffer.put(command.command);
        buffer.put(message.node().ip().getAddress());
        buffer.putInt(message.node().port());
        buffer.putInt(message.id());

        return buffer.array();
    }
}
