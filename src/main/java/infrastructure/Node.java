package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.converter.StartPayloadConverter;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.LeaderContext;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node {
    private final static Logger LOG = LogManager.getLogger(Node.class);
    private final PayloadConverter<StartMessage> payloadConverter = new StartPayloadConverter();

    public final SystemContext context;
    private final RemoteClient<DatagramPacket> defaultClient;
    private final RequestHandler<DatagramPacket> defaultClientRequestHandler;
    private final RemoteClient<byte[]> reliableClient;
    private final RequestHandler<byte[]> reliableClientRequestHandler;

    public Node(Configuration configuration) {
        this.context = configuration.getContext();
        this.defaultClient = configuration.getDefaultClient();
        this.defaultClientRequestHandler = configuration.getDefaultClientRequestHandler();
        this.reliableClient = configuration.getReliableClient();
        this.reliableClientRequestHandler = configuration.getReliableClientRequestHandler();

        if (context.isLeader()) {
            setupLeader(context);
        }
    }

    public void joinSystem() throws IOException {
        LOG.info(context.id + " joins the system");

        defaultClient.listen(context, defaultClientRequestHandler, context.listenPort);
        reliableClient.listen(context, reliableClientRequestHandler, context.listenPort);

        defaultClient.broadcast(payloadConverter.encode(Command.START, new StartMessage(InetAddress.getLocalHost(), context.listenPort)));
    }

    private void setupLeader(SystemContext context) {
        context.setLeaderContext(new LeaderContext());

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.scheduleAtFixedRate(() -> {
            HashMap<RemoteNode, Integer> aliveNodes = context.getLeaderContext().aliveNodes;
            aliveNodes.forEach((key, value) -> aliveNodes.replace(key, value + 1));
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void shutdown() throws IOException {
        defaultClient.close();
        reliableClient.close();

        Thread.currentThread().interrupt();
    }
}
