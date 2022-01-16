package infrastructure.converter;

import infrastructure.system.Leader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class StartAckPayloadConverter implements PayloadConverter<Leader> {
    @Override
    public Leader convert(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            int leaderPort = buffer.getInt();
            byte[] leaderIp = new byte[4 * Byte.BYTES];
            buffer.get(leaderIp);
            InetAddress leader = InetAddress.getByAddress(leaderIp);

            return new Leader(leader, leaderPort);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
