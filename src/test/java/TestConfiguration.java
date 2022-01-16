import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import org.mockito.Mockito;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static infrastructure.system.IdService.nodeId;

public class TestConfiguration extends Configuration {
    private final int port;
    private final Leader leader;
    private final RemoteClient<DatagramPacket> remoteClient;

    public TestConfiguration(int port, Leader leader) {
        this.port = port;
        this.leader = leader;
        this.remoteClient =  Mockito.spy(UdpClient.class);
    }

    @Override
    public SystemContext getContext() {
        try {
            SystemContext context = new SystemContext(nodeId(InetAddress.getLocalHost(), port), port);
            context.setLeader(leader);
            return context;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RemoteClient<DatagramPacket> getRemoteClient() {
        return remoteClient;
    }
}
