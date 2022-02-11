package infrastructure.system;

import java.net.DatagramPacket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ReliableClientContext {
    public final Map<RemoteNode, Integer> sendSequences;
    public final Map<RemoteNode, Integer> receiveSequences;
    public final Map<RemoteNode, Queue<Pair<Integer, byte[]>>> previousMessages;
    public final Queue<Pair<Integer, DatagramPacket>> holdBackQueue;

    public ReliableClientContext() {
        this.sendSequences = new HashMap<>();
        this.receiveSequences = new HashMap<>();
        this.previousMessages = new HashMap<>();
        this.holdBackQueue = new ArrayDeque<>();
    }
}
