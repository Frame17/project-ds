package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.IdService;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.NeighbourInfoMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class NeighbourInfoMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(NeighbourInfoMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<NeighbourInfoMessage> converter;

    public NeighbourInfoMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<NeighbourInfoMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        NeighbourInfoMessage message = converter.decode(packet.getData());

        if (context.isLeader()) {
            List<AbstractMap.SimpleImmutableEntry<String, RemoteNode>> nodes = context.getLeaderContext().aliveNodes.keySet().stream()
                    .map(it -> new AbstractMap.SimpleImmutableEntry<>(IdService.nodeId(it.ip(), it.port()), it))
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            int i = nodes.stream().map(Map.Entry::getKey).toList().indexOf(IdService.nodeId(message.neighbour().ip(), message.neighbour().port()));
            RemoteNode newNeighbour = nodes.get((i + 1) % nodes.size()).getValue();
            try {
                if (newNeighbour.ip() != message.neighbour().ip() && newNeighbour.port() != message.neighbour().port()) {
                    client.unicast(converter.encode(Command.NEIGHBOUR_INFO, new NeighbourInfoMessage(new RemoteNode(newNeighbour.ip(), newNeighbour.port()))),
                            message.neighbour().ip(), message.neighbour().port());
                } else {
                    client.unicast(converter.encode(Command.NEIGHBOUR_INFO, new NeighbourInfoMessage(null)),
                            message.neighbour().ip(), message.neighbour().port());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            LOG.info(context.id + " sets neighbour {}", message.neighbour());
            context.setNeighbour(message.neighbour());
        }
    }
}
