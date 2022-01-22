package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.LeaderInfoMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class LeaderInfoPayloadConverter implements PayloadConverter<LeaderInfoMessage> {

    @Override
    public LeaderInfoMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);

            byte[] neighbourIp = new byte[4 * Byte.BYTES];

            buffer.get(neighbourIp);
            int port = buffer.getInt();

            InetAddress neighbour = InetAddress.getByAddress(neighbourIp);

            return new LeaderInfoMessage(neighbour, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command c, LeaderInfoMessage record) {
        byte[] address = record.neighbour().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length + Integer.BYTES);

        buffer.put(c.command);
        buffer.put(address);
        buffer.putInt(record.port());

        return buffer.array();
    }
}
