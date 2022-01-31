package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.Leader;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.StartAckMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class StartAckPayloadConverter implements PayloadConverter<StartAckMessage> {

    @Override
    public StartAckMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            byte[] leaderIp = new byte[4 * Byte.BYTES];
            buffer.get(leaderIp);
            int leaderPort = buffer.getInt();
            byte[] neighbourIp = new byte[4 * Byte.BYTES];
            buffer.get(neighbourIp);

            return new StartAckMessage(new Leader(InetAddress.getByAddress(leaderIp), leaderPort),
                    new RemoteNode(InetAddress.getByAddress(neighbourIp), buffer.getInt()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, StartAckMessage message) {
        byte[] leaderIp = message.leader().ip().getAddress();
        byte[] neighbourIp = message.neighbour().ip().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + leaderIp.length + Integer.BYTES + neighbourIp.length + Integer.BYTES);

        buffer.put(command.command);
        buffer.put(leaderIp);
        buffer.putInt(message.leader().port());
        buffer.put(neighbourIp);
        buffer.putInt(message.neighbour().port());

        return buffer.array();
    }
}
