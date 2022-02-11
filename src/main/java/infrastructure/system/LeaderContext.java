package infrastructure.system;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

public class LeaderContext {
    /**
     * Defines a correspondence node -> liveliness counter.
     * When the counter is greater than a retry number - the node is considered dead.
     */
    public final HashMap<RemoteNode, Integer> aliveNodes = new HashMap<>();

    /**
     * Defines a correspondence file -> nodes where it's chunks are stored.
     * There may be several FileChunks in the list with the same RemoteNode. This is done to support replication.
     */
    public final HashMap<String, List<FileChunk>> chunksDistributionTable = new HashMap<>();

    /**
     * Defines a mapping, between a RemoteNode (here client) which has "requested" a file, to many FileRequests
     * It can happen, that a RemoteNode(client) requests multiple files at the same time...
     */
    public final HashMap<RemoteNode, List<FileRequest>> fileReadRequest = new HashMap<>();
}
