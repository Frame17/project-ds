package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.NeighbourInfoMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class LeaderInfoPayloadConverter implements PayloadConverter<NeighbourInfoMessage> {

    @Override
    public NeighbourInfoMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            byte[] neighbourIp = new byte[4 * Byte.BYTES];

            buffer.get(neighbourIp);
            int port = buffer.getInt();

            return new NeighbourInfoMessage(InetAddress.getByAddress(neighbourIp), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, NeighbourInfoMessage payload) {
        byte[] address = payload.neighbour().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length + Integer.BYTES);

        buffer.put(command.command);
        buffer.put(address);
        buffer.putInt(payload.port());

        return buffer.array();
    }
}
