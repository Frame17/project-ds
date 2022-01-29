package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.converter.ElectionPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.converter.StartPayloadConverter;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.RemoteNode;
import infrastructure.system.LeaderContext;
import infrastructure.system.SystemContext;
import infrastructure.system.message.ElectionMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

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
            context.setLeaderContext(new LeaderContext());
        }
    }

    public void joinSystem() throws IOException {
        LOG.info(context.id + " joins the system");

        defaultClient.listen(context, defaultClientRequestHandler, context.listenPort);
        reliableClient.listen(context, reliableClientRequestHandler, context.listenPort);

        defaultClient.broadcast(payloadConverter.encode(Command.START, new StartMessage(context.listenPort)));
    }

    public void shutdown() throws IOException {
        defaultClient.close();
        reliableClient.close();

        Thread.currentThread().interrupt();
    }

    // todo - move to HealthMessageHandler after implementation
    public void startMasterElection(SystemContext context) {
        try {
            LOG.info("Start election");
            context.setElectionParticipant(true);
            ElectionMessage message = new ElectionMessage(InetAddress.getLocalHost(), false);

            defaultClient.unicast(new ElectionPayloadConverter().encode(Command.ELECTION, message),
                    context.getNeighbour().ip(), context.getNeighbour().port());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public void sendHealthMassage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream send;
        //how to get the nodes RemoteNode Object?
        RemoteNode myRemoteNode = null;
        try{
            send = new ObjectOutputStream(baos);
            send.writeObject(myRemoteNode);
            send.flush();
            byte[] remoteNodeBytes = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + remoteNodeBytes.length);
            buffer.put(Command.HEALTH.command);
            buffer.put(remoteNodeBytes);
            defaultClient.unicast(buffer.array(), context.getLeader().leaderIp(), context.listenPort);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            baos.close();
        }

    }
}
