package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<Integer> converter;
    private HashMap<Integer, Integer> HealthStatus = new HashMap<Integer, Integer>() ;

    public HealthMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<Integer> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        if (context.isLeader()) {
            // to-do: add packet contains nodeID of a node, add the node id to the hashmap and initialise the counter with 0
            // only add nodeID to the hashMap if it is not already in there
            // todo change NodeID to Remote node
            Integer nodeID = converter.convert(packet.getData());
            if (HealthStatus.containsKey(nodeID)){
                HealthStatus.replace(nodeID, 0);
            }else{
                HealthStatus.put(nodeID, 0);
            }
            incrementCounter();
        }
    }
    public void incrementCounter(){
        //every 3 seconds each value for the corresponding key gets increased by one
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.schedule(() -> {
            try {
                for (HashMap.Entry<Integer, Integer> i : HealthStatus.entrySet()){
                    HealthStatus.replace(i.getKey(), i.getValue() +1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3, TimeUnit.SECONDS);
    }
}
