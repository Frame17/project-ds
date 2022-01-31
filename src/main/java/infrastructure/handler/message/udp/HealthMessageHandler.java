package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.HealthMessage;

import java.io.IOException;
import java.net.DatagramPacket;

public class HealthMessageHandler implements UdpMessageHandler {
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<HealthMessage> converter;

    public HealthMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<HealthMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        if (context.isLeader()) {
            HealthMessage message = converter.decode(packet.getData());
            context.getLeaderContext().aliveNodes.put(new RemoteNode(packet.getAddress(), message.port()), 0);

            try {
                client.unicast(new byte[]{Command.HEALTH_ACK.command}, context.getLeader().ip(), context.getLeader().port());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
