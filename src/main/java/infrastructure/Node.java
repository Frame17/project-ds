package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Node {
    public final short listenPort;
    public final SystemContext context;
    private final RemoteClient<DatagramPacket> client;
    private final RequestHandler<DatagramPacket> requestHandler;

    public Node(Configuration configuration) {
        this.listenPort = configuration.getListenPort();
        this.context = configuration.getContext();
        this.client = configuration.getRemoteClient();
        this.requestHandler = configuration.getRequestHandler();
    }

    public void joinSystem() throws IOException {
        client.listen(context, requestHandler, listenPort);

        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(Command.START.command);
        buffer.putShort(listenPort);
        client.broadcast(buffer.array());
    }

    public void startMasterElection() {

    }

    public boolean isLeader() {
        try {
            return context.getLeader().equals(new Leader(InetAddress.getLocalHost(), listenPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
