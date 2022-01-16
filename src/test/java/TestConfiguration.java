import configuration.Configuration;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestConfiguration extends Configuration {
    private final short port;
    private final Leader leader;

    public TestConfiguration(short port, Leader leader) {
        this.port = port;
        this.leader = leader;
    }

    @Override
    public short getListenPort() {
        return port;
    }

    @Override
    public SystemContext getContext() {
        try {
            SystemContext context = new SystemContext(InetAddress.getLocalHost().getHostAddress() + ':' + getListenPort());
            context.setLeader(leader);
            return context;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
