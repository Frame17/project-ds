package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import static infrastructure.system.IdService.nodeId;

public class StartMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<Integer> converter;

    public StartMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<Integer> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            int port = converter.convert(packet.getData());

            if (!context.id.equals(nodeId(packet.getAddress(), port))) {    // don't reply to own request
                client.unicast(buildMessage(context), packet.getAddress(), port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] buildMessage(SystemContext context) {
        byte[] address = context.getLeader().leaderIp().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + address.length);

        buffer.put(Command.START_ACK.command);
        buffer.putInt(context.getLeader().leaderPort());
        buffer.put(address);

        return buffer.array();
    }
}
