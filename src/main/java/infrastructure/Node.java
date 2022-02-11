package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.converter.NeighbourInfoPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.converter.StartPayloadConverter;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.IdService;
import infrastructure.system.LeaderContext;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.NeighbourInfoMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node {
    private final static Logger LOG = LogManager.getLogger(Node.class);
    private final PayloadConverter<StartMessage> startConverter = new StartPayloadConverter();
    private final PayloadConverter<NeighbourInfoMessage> neighbourInfoConverter = new NeighbourInfoPayloadConverter();

    public final SystemContext context;
    private final RemoteClient<DatagramPacket> defaultClient;
    private final RequestHandler<DatagramPacket> defaultClientRequestHandler;
    private final RemoteClient<DatagramPacket> reliableClient;
    private final RequestHandler<DatagramPacket> reliableClientRequestHandler;

    public Node(Configuration configuration) {
        this.context = configuration.getContext();
        this.defaultClient = configuration.getDefaultClient();
        this.defaultClientRequestHandler = configuration.getDefaultClientRequestHandler();
        this.reliableClient = configuration.getReliableClient();
        this.reliableClientRequestHandler = configuration.getFileOperationsRequestHandler();

        if (context.isLeader()) {
            setupLeader(context);
        }
    }

    public void joinSystem() throws IOException {
        LOG.info(context.id + " joins the system");

        defaultClient.listen(context, defaultClientRequestHandler, context.listenPort);
        reliableClient.listen(context, reliableClientRequestHandler, context.filesListenPort);

        defaultClient.broadcast(startConverter.encode(Command.START, new StartMessage(InetAddress.getLocalHost(), context.listenPort)));
    }

    private void setupLeader(SystemContext context) {
        context.setLeaderContext(new LeaderContext());

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.scheduleAtFixedRate(() -> {
            HashMap<RemoteNode, Integer> aliveNodes = context.getLeaderContext().aliveNodes;
            aliveNodes.forEach((node, healthCounter) -> {
                if (healthCounter > 3) {
                    if (aliveNodes.size() > 1) {
                        reassignNeighbour(aliveNodes, node);
                    }

                    aliveNodes.remove(node);
                } else {
                    aliveNodes.replace(node, healthCounter + 1);
                }
            });

        }, 0, 3, TimeUnit.SECONDS);
    }

    private void reassignNeighbour(Map<RemoteNode, Integer> aliveNodes, RemoteNode lostNode) {
        List<AbstractMap.SimpleImmutableEntry<String, RemoteNode>> nodes = aliveNodes.keySet().stream()
                .map(it -> new AbstractMap.SimpleImmutableEntry<>(IdService.nodeId(it.ip(), it.port()), it))
                .sorted(Map.Entry.comparingByKey())
                .toList();

        int i = nodes.stream().map(Map.Entry::getKey).toList().indexOf(IdService.nodeId(lostNode.ip(), lostNode.port()));
        if (aliveNodes.size() == 2) {
            try {
                RemoteNode target = nodes.get((i + 1) % nodes.size()).getValue();
                defaultClient.unicast(neighbourInfoConverter.encode(Command.NEIGHBOUR_INFO, new NeighbourInfoMessage(null)),
                        target.ip(), target.port());
            } catch (IOException e) {
                LOG.error(e);
            }
        } else {
            try {
                RemoteNode target = nodes.get(Math.floorMod((i - 1), nodes.size())).getValue();
                RemoteNode newNeighbour = nodes.get((i + 1) % nodes.size()).getValue();
                defaultClient.unicast(neighbourInfoConverter.encode(Command.NEIGHBOUR_INFO,
                                new NeighbourInfoMessage(new RemoteNode(newNeighbour.ip(), newNeighbour.port()))),
                        target.ip(), target.port());
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    public void shutdown() throws IOException {
        defaultClient.close();
        reliableClient.close();
    }
}
