package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import infrastructure.system.message.StartAckMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartAckMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(StartAckMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<StartAckMessage> converter;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<StartAckMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        if (context.getLeader() == null) {
            StartAckMessage message = converter.decode(packet.getData());
            Leader leader = message.leader();

            LOG.info("Set new leader {}:{}", leader.leaderIp(), leader.leaderPort());
            context.setLeader(leader);

            startHealthCheck(context);
        }
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
