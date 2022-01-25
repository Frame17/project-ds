package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.converter.ElectionPayloadConverter;
import infrastructure.converter.StartPayloadConverter;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.message.ElectionMassage;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class Node {
    private final static Logger LOG = LogManager.getLogger(Node.class);

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
    }

    public void joinSystem() throws IOException {
        LOG.info(context.id + " joins the system");

        defaultClient.listen(context, defaultClientRequestHandler, context.listenPort);
        reliableClient.listen(context, reliableClientRequestHandler, context.listenPort);

        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
        buffer.put(Command.START.command);
        buffer.putInt(context.listenPort);
        defaultClient.broadcast(buffer.array());
        // May introduce extra thread
        int loopCount = 0;

        // Send the Start-Message 5 times, if no leader ist detected, this node is the Leader
        while (context.getLeader() == null && loopCount < 5){
            loopCount++;

            StartMessage startMessage = new StartMessage(context.listenPort);
            defaultClient.broadcast(new StartPayloadConverter().encode(Command.START, startMessage));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(context.getLeader() == null){
            context.setLeader(new Leader(context.getLocalAddress(), context.getListenPort()));
            context.actAsLeader();

            //Hihi, start new election ;)
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {

                try {
                    LOG.info("Start election");
                    ElectionMassage message = new ElectionMassage(InetAddress.getByName("172.16.0.1"), false);

                    // Send Message to random node
                    defaultClient.unicast(
                            new ElectionPayloadConverter().encode(Command.ELECTION, message),
                            context.getNodes().get((int)(Math.random()* context.getNodes().size())).ip(),
                            DEFAULT_LISTEN_PORT
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }, 1, TimeUnit.MINUTES);
        }
    }

    public void startMasterElection() {

    }
}
