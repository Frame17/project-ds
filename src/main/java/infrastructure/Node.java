package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.handler.request.RequestHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {
    public static final int LISTEN_PORT = 5000;
    public final SystemContext context = new SystemContext();
    private final RemoteClient<DatagramPacket> client;
    private final RequestHandler<DatagramPacket> requestHandler;

    public Node(Configuration configuration) {
        this.client = configuration.getRemoteClient();
        this.requestHandler = configuration.getRequestHandler();
    }

    public void joinSystem() throws IOException {
        client.listen(LISTEN_PORT, context, requestHandler);
        client.broadcast(new byte[]{Command.START.command});
    }

    public void startMasterElection() {

    }

    public boolean isLeader() {
        try {
            return context.getLeader().equals(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
