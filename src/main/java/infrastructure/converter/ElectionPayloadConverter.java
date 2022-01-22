package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.ElectionMassage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ElectionPayloadConverter implements PayloadConverter<ElectionMassage> {

    @Override
    public ElectionMassage decode(byte[] payload) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);

            byte[] ipB = new byte[4 * Byte.BYTES];
            buffer.get(ipB);

            int isLeader = buffer.getInt();

            InetAddress ip = InetAddress.getByAddress(ipB);
            ElectionMassage electionMassage = new ElectionMassage(ip, isLeader == 1);

            return electionMassage;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Command c, ElectionMassage record) {
        byte[] address = record.mid().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + address.length);

        buffer.put(c.command);
        buffer.put(address);
        buffer.putInt(record.isLeader()?1:0);


        return buffer.array();
    }
}
