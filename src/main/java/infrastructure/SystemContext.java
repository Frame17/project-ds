package infrastructure;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemContext {
    private InetAddress leader;
    public final AtomicInteger healthCounter = new AtomicInteger();

    public InetAddress getLeader() {
        return leader;
    }

    public void setLeader(InetAddress leader) {
        this.leader = leader;
    }
}
