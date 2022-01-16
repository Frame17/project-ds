package infrastructure;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Node {

    private final static Logger LOG = LogManager.getLogger(Node.class);

    public final SystemContext context;
    private final RemoteClient<DatagramPacket> client;
    private final RequestHandler<DatagramPacket> requestHandler;

    public Node(Configuration configuration) {
        this.context = configuration.getContext();
        this.client = configuration.getRemoteClient();
        this.requestHandler = configuration.getRequestHandler();
    }

    public void joinSystem() throws IOException {
        LOG.info("Join system");

        client.listen(context, requestHandler, context.listenPort);

        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
        buffer.put(Command.START.command);
        buffer.putInt(context.listenPort);
        client.broadcast(buffer.array());
    }

    public void startMasterElection() {

    }
}
