package infrastructure.handler.message.udp;

import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;
import java.util.HashMap;

public class HealthMessageHandler implements UdpMessageHandler {
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
            //

        }
    }
}
