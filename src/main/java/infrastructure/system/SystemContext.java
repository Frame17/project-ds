package infrastructure.system;

import infrastructure.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemContext {
    private final static Logger LOG = LogManager.getLogger(SystemContext.class);
    public final String id;
    private Node self;
    public final int listenPort;
    public final int filesListenPort;
    private Leader leader;
    private LeaderContext leaderContext;
    public final AtomicInteger healthCounter = new AtomicInteger();
    private RemoteNode neighbour;
    private boolean electionParticipant;
    private final ReliableClientContext reliableClientContext = new ReliableClientContext();

    public SystemContext(String id, int listenPort, int filesListenPort) {
        this.id = id;
        this.listenPort = listenPort;
        this.filesListenPort = filesListenPort;
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public boolean isLeader() {
        try {
            return leader != null && leader.equals(new Leader(InetAddress.getLocalHost(), listenPort));
        } catch (UnknownHostException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public LeaderContext getLeaderContext() {
        return leaderContext;
    }

    public void setLeaderContext(LeaderContext leaderContext) {
        this.leaderContext = leaderContext;
    }

    public RemoteNode getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(RemoteNode neighbour) {
        this.neighbour = neighbour;
    }

    public boolean isElectionParticipant() {
        return electionParticipant;
    }

    public void setElectionParticipant(boolean electionParticipant) {
        this.electionParticipant = electionParticipant;
    }

    public ReliableClientContext getReliableClientContext() {
        return reliableClientContext;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }
}
