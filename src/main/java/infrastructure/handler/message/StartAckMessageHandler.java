package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartAckMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<Leader> converter;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<Leader> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        context.setLeader(converter.convert(packet.getData()));
        startHealthCheck(context);
    }

    private void startHealthCheck(SystemContext context) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.schedule(() -> {
            try {
                client.unicast(new byte[]{Command.HEALTH.command}, context.getLeader().leaderIp(), context.getLeader().leaderPort());
                context.healthCounter.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
