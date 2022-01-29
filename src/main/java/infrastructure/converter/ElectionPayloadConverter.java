package infrastructure.converter;

import infrastructure.Command;
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
            boolean isLeader = buffer.get() == 1;

            return new ElectionMessage(InetAddress.getByAddress(ip), isLeader);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, ElectionMessage payload) {
        byte[] address = payload.candidate().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length);
        buffer.put(command.command);
        buffer.put(address);

        return buffer.array();
    }
}
