package infrastructure.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class SystemContext {
    private final static Logger LOG = LogManager.getLogger(SystemContext.class);

    public final String id;
    public final int listenPort;
    private Leader leader;
    public final AtomicInteger healthCounter = new AtomicInteger();

    public SystemContext(String id, int listenPort) {
        this.id = id;
        this.listenPort = listenPort;
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public boolean isLeader() {
        try {
            return leader.equals(new Leader(InetAddress.getLocalHost(), listenPort));
        } catch (UnknownHostException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public InetAddress getLocalAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    public int getListenPort(){
        return listenPort;
    }


    // election stuff
    //TODO: REFACTOR :(
    public InetAddress neighbour;
    public Direction direction;
    public List<InetAddress> nodes = new ArrayList<>();


    public void actAsLeader(){
        LOG.info("This node is now acting as leader {}", getLeader());



        //TODO: Leader has to know each node, and form a ring...
    }



    public enum Direction {
        LEFT(0), RIGHT(1);

        public final byte direction;

        Direction(int direction) {
            this.direction = ((byte) direction);
        }
    }
}
