import infrastructure.Node;
import infrastructure.client.UdpClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * This component is made for local testing of a distributed setup
 */
public class TestUdpClient extends UdpClient {
    public List<Node> system;

    @Override
    public void broadcast(byte[] message) {
        system.forEach(node -> {
            try {
                super.unicast(message, InetAddress.getLocalHost(), node.context.listenPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
