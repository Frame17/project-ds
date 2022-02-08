package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.HealthMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;

public class HealthMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(HealthMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<HealthMessage> converter;

    public HealthMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<HealthMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet, RemoteNode sender) {
        if (context.isLeader()) {
            HealthMessage message = converter.decode(packet.getData());
            context.getLeaderContext().aliveNodes.put(message.node(), 0);

            try {
                client.unicast(new byte[]{Command.HEALTH_ACK.command}, message.node().ip(), message.node().port());
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }
}
