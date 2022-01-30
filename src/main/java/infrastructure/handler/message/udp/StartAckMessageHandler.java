package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.ElectionPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import infrastructure.system.message.ElectionMessage;
import infrastructure.system.message.HealthMessage;
import infrastructure.system.message.StartAckMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartAckMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(StartAckMessageHandler.class);
    private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<StartAckMessage> converter;
    private final PayloadConverter<HealthMessage> healthPayloadConverter;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<StartAckMessage> converter, PayloadConverter<HealthMessage> healthPayloadConverter) {
        this.client = client;
        this.converter = converter;
        this.healthPayloadConverter = healthPayloadConverter;
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
        LOG.info("Start health executor for " + context.id);
        threadPool.scheduleAtFixedRate(() -> {
            try {
                LOG.info(context.id + " sends health message to {}:{}", context.getLeader().leaderIp(), context.getLeader().leaderPort());

                client.unicast(healthPayloadConverter.encode(Command.HEALTH, new HealthMessage(context.listenPort)),
                        context.getLeader().leaderIp(), context.getLeader().leaderPort());
                int leaderHealthCounter = context.healthCounter.incrementAndGet();
                if (leaderHealthCounter > 3) {
                    startLeaderElection(context);
                }
            } catch (IOException e) {
                LOG.error(e);
                throw new RuntimeException(e);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void startLeaderElection(SystemContext context) {
        try {
            LOG.info(context.id + " starts leader election");
            context.setElectionParticipant(true);
            ElectionMessage message = new ElectionMessage(InetAddress.getLocalHost(), false);

            client.unicast(new ElectionPayloadConverter().encode(Command.ELECTION, message),
                    context.getNeighbour().ip(), context.getNeighbour().port());
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
