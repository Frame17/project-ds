package infrastructure.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemContext {
    private final static Logger LOG = LogManager.getLogger(SystemContext.class);

    public final String id;
    public final int listenPort;
    private Leader leader;
    private LeaderContext leaderContext;
    public final AtomicInteger healthCounter = new AtomicInteger();

    public SystemContext(String id, int listenPort) {
        this.id = id;
        this.listenPort = listenPort;
    }

    public boolean isLeader() {
        try {
            return leader.equals(new Leader(InetAddress.getLocalHost(), listenPort));
        } catch (UnknownHostException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public LeaderContext getLeaderContext() {
        return leaderContext;
    }

    public void setLeaderContext(LeaderContext leaderContext) {
        this.leaderContext = leaderContext;
    }
}
