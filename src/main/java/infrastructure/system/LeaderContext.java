package infrastructure.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderContext {
    /**
     * Defines a correspondence client -> liveliness counter.
     * When the counter is greater than a retry number - the client is considered dead.
     */
    public final Map<RemoteNode, Integer> aliveNodes = new ConcurrentHashMap<>();

    /**
     * Defines a correspondence file -> nodes where it's chunks are stored.
     * There may be several FileChunks in the list with the same RemoteNode. This is done to support replication.
     */
    public final HashMap<String, List<FileChunk>> chunksDistributionTable = new HashMap<>();

    public final HashMap<String, Pair<FileReadRequest, List<RemoteNode>>> fileReadRequests = new HashMap<>();
}
