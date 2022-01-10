package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.SystemContext;
import infrastructure.client.RemoteClient;

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
            byte[] address = context.getLeader().getAddress();
            ByteBuffer buffer = ByteBuffer.allocate(1 + address.length);
            buffer.put(Command.START_ACK.command);
            buffer.put(address);

            client.unicast(buffer.array(), packet.getAddress(), packet.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
