import configuration.Configuration;
import infrastructure.Node;
import infrastructure.client.RemoteClient;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import org.mockito.Mockito;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static infrastructure.system.IdService.nodeId;

public class TestConfiguration extends Configuration {
    private final int port;
    private final Leader leader;
    private final RemoteClient<DatagramPacket> remoteClient;

    public TestConfiguration(int port, Leader leader, List<Node> system) {
        super(new String[]{});
        this.port = port;
        this.leader = leader;
        TestUdpClient spy = Mockito.spy(TestUdpClient.class);
        spy.system = system;
        this.remoteClient = spy;
    }

    @Override
    public SystemContext getContext() {
        SystemContext context = new SystemContext(nodeId(Node.getLocalIp(), port), port, port + 1);
        context.setLeader(leader);
        return context;
    }

    @Override
    public RemoteClient<DatagramPacket> getDefaultClient() {
        return remoteClient;
    }
}
