package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

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
    }

    public void startMasterElection() {

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
