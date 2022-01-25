package infrastructure.system;

import java.util.HashMap;

public class LeaderContext {
    public final HashMap<RemoteNode, Integer> aliveNodes = new HashMap<>();
    public final HashMap<RemoteNode, String> chunksDistributionTable = new HashMap<>();
}
