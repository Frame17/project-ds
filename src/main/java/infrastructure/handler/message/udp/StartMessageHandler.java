package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.StartAckMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;

public class StartMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(StartMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<StartMessage> startConverter;
    private final PayloadConverter<StartAckMessage> startAckConverter;

    public StartMessageHandler(
            RemoteClient<DatagramPacket> client,
            PayloadConverter<StartMessage> startConverter,
            PayloadConverter<StartAckMessage> startAckConverter
    ) {
        this.client = client;
        this.startConverter = startConverter;
        this.startAckConverter = startAckConverter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            StartMessage startMessage = startConverter.decode(packet.getData());
            client.unicast(startAckConverter.encode(Command.START_ACK, new StartAckMessage(context.getLeader())),
                    packet.getAddress(), startMessage.port());

            if (context.isLeader()) {
                context.getLeaderContext().aliveNodes.put(new RemoteNode(packet.getAddress(), startMessage.port()), 0);
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
