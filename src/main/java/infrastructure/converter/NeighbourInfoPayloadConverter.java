package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.NeighbourInfoMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NeighbourInfoPayloadConverter implements PayloadConverter<NeighbourInfoMessage> {

    @Override
    public NeighbourInfoMessage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
            byte[] neighbourIp = new byte[4 * Byte.BYTES];

            buffer.get(neighbourIp);
            if (Arrays.equals(neighbourIp, new byte[]{0, 0, 0, 0})) {
                return new NeighbourInfoMessage(null);
            } else {
                int port = buffer.getInt();
                return new NeighbourInfoMessage(new RemoteNode(InetAddress.getByAddress(neighbourIp), port));
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command command, NeighbourInfoMessage message) {
        byte[] result;
        if (message.neighbour() == null) {
            result = new byte[]{command.command};
        } else {
            byte[] address = message.neighbour().ip().getAddress();
            ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + address.length + Integer.BYTES);

            buffer.put(command.command);
            buffer.put(address);
            buffer.putInt(message.neighbour().port());
            result = buffer.array();
        }

        return result;
    }
}
