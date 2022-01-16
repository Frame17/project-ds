package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartAckMessageHandler implements MessageHandler {

    private final static Logger LOG = LogManager.getLogger(StartAckMessageHandler.class);

    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<Leader> converter;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<Leader> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        Leader leader = converter.convert(packet.getData());

        LOG.info("Set new leader {}:{}", leader.leaderIp(), leader.leaderPort());

        context.setLeader(leader);
        startHealthCheck(context);
    }

    private void startHealthCheck(SystemContext context) {
        LOG.info("Start health executor");
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.schedule(() -> {
            try {
                LOG.info("Send health message to {}:{}", context.getLeader().leaderIp(), context.getLeader().leaderPort());

                client.unicast(new byte[]{Command.HEALTH.command}, context.getLeader().leaderIp(), context.getLeader().leaderPort());
                context.healthCounter.incrementAndGet();
            } catch (IOException e) {
                LOG.error(e);
                throw new RuntimeException(e);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
