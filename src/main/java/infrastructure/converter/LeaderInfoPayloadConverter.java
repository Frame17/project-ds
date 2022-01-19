package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.LeaderInfoMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class LeaderInfoPayloadConverter implements PayloadConverter<LeaderInfoMessage> {

    @Override
    public LeaderInfoMessage convert(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);

            byte[] leaderIp = new byte[4 * Byte.BYTES];
            buffer.get(leaderIp);
            InetAddress leader = InetAddress.getByAddress(leaderIp);

            return new LeaderInfoMessage(leader);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LeaderInfoMessage decode(byte[] payload) {
        return this.convert(payload);
    }

    @Override
    public byte[] encode(Command c, LeaderInfoMessage record) {
        byte[] address = record.neighbour().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length);

        buffer.put(c.command);
        buffer.put(address);

        return buffer.array();
    }
}
