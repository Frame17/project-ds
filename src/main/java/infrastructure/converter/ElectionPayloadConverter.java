package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.ElectionMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ElectionPayloadConverter implements PayloadConverter<ElectionMessage> {

    @Override
    public ElectionMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            byte[] ip = new byte[4 * Byte.BYTES];
            buffer.get(ip);
            int port = buffer.getInt();
            boolean isLeader = buffer.get() == 1;

            return new ElectionMessage(new RemoteNode(InetAddress.getByAddress(ip), port), isLeader);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, ElectionMessage message) {
        byte[] address = message.candidate().ip().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length + Integer.BYTES + Byte.BYTES);
        buffer.put(command.command);
        buffer.put(address);
        buffer.putInt(message.candidate().port());
        buffer.put((byte) (message.isLeader() ? 1 : 0));

        return buffer.array();
    }
}
