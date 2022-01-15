package infrastructure.system;

import java.util.concurrent.atomic.AtomicInteger;

public class SystemContext {
    public final String id;
    private Leader leader;
    public final AtomicInteger healthCounter = new AtomicInteger();

    public SystemContext(String id) {
        this.id = id;
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }
}
