package infrastructure.handler.message;

import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;

public class HealthMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<Leader> converter;

    public HealthMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<Leader> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        if (context.isLeader()) {
            // todo - implement
        }
    }
}
