package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class StartMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;

    public StartMessageHandler(RemoteClient<DatagramPacket> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            ByteBuffer message = ByteBuffer.wrap(packet.getData());
            short port = message.getShort(1);

            client.unicast(buildMessage(context), packet.getAddress(), port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] buildMessage(SystemContext context) {
        byte[] address = context.getLeader().leaderIp().getAddress();
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + address.length);

        buffer.put(Command.START_ACK.command);
        buffer.putShort(context.getLeader().leaderPort());
        buffer.put(address);

        return buffer.array();
    }
}
