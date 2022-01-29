package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.Leader;
import infrastructure.system.message.StartAckMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class StartAckPayloadConverter implements PayloadConverter<StartAckMessage> {

    @Override
    public StartAckMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            int leaderPort = buffer.getInt();
            byte[] leaderIp = new byte[4 * Byte.BYTES];
            buffer.get(leaderIp);
            InetAddress leader = InetAddress.getByAddress(leaderIp);

            return new StartAckMessage(new Leader(leader, leaderPort));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, StartAckMessage payload) {
        byte[] address = payload.leader().leaderIp().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + address.length);

        buffer.put(command.command);
        buffer.putInt(payload.leader().leaderPort());
        buffer.put(address);

        return buffer.array();
    }


}
