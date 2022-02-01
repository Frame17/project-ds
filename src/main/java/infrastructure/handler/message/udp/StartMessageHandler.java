package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.IdService;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.NeighbourInfoMessage;
import infrastructure.system.message.StartAckMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class StartMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(StartMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<StartMessage> startConverter;
    private final PayloadConverter<StartAckMessage> startAckConverter;
    private final PayloadConverter<NeighbourInfoMessage> neighbourInfoConverter;

    public StartMessageHandler(
            RemoteClient<DatagramPacket> client,
            PayloadConverter<StartMessage> startConverter,
            PayloadConverter<StartAckMessage> startAckConverter,
            PayloadConverter<NeighbourInfoMessage> neighbourInfoConverter
    ) {
        this.client = client;
        this.startConverter = startConverter;
        this.startAckConverter = startAckConverter;
        this.neighbourInfoConverter = neighbourInfoConverter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            StartMessage startMessage = startConverter.decode(packet.getData());

            if (!context.id.equals(IdService.nodeId(startMessage.ip(), startMessage.port()))) {      // do not handle own broadcast message
                RemoteNode sender = new RemoteNode(startMessage.ip(), startMessage.port());

                if (context.isLeader()) {
                    client.unicast(startAckConverter.encode(Command.START_ACK, new StartAckMessage(context.getLeader())),
                            startMessage.ip(), startMessage.port());

                    context.getLeaderContext().aliveNodes.put(sender, 0);
                } else {
                    RemoteNode current = new RemoteNode(InetAddress.getLocalHost(), context.listenPort);

                    if (context.getNeighbour() == null) {
                        client.unicast(neighbourInfoConverter.encode(Command.NEIGHBOUR_INFO, new NeighbourInfoMessage(current)),
                                startMessage.ip(), startMessage.port());
                        context.setNeighbour(sender);
                    } else if (senderIpBetweenCurrentAndNeighbour(context, current, sender)) {
                        client.unicast(neighbourInfoConverter.encode(Command.NEIGHBOUR_INFO, new NeighbourInfoMessage(context.getNeighbour())),
                                startMessage.ip(), startMessage.port());
                        context.setNeighbour(sender);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private boolean senderIpBetweenCurrentAndNeighbour(SystemContext context, RemoteNode current, RemoteNode sender) {
        return IdService.nodeId(current.ip(), current.port())
                .compareTo(IdService.nodeId(sender.ip(), sender.port())) > 0
                && IdService.nodeId(context.getNeighbour().ip(), context.getNeighbour().port())
                .compareTo(IdService.nodeId(sender.ip(), sender.port())) < 0;
    }
}
