package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.RemoteClient;
import infrastructure.converter.ElectionPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.LeaderContext;
import infrastructure.system.RemoteNode;
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
        StartAckMessage message = converter.decode(packet.getData());

        LOG.info(context.id + " sets new leader {}:{}", message.leader().ip(), message.leader().port());
        context.setLeader(new Leader(message.leader().ip(), message.leader().port()));

        startHealthCheck(context);
    }

    private void startHealthCheck(SystemContext context) {
        LOG.info(context.id + " starts health executor");
        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (!context.isLeader()) {
                    LOG.info(context.id + " sends health message to {}:{}", context.getLeader().ip(), context.getLeader().port());

                    client.unicast(healthPayloadConverter.encode(Command.HEALTH, new HealthMessage(new RemoteNode(Node.getLocalIp(), context.listenPort))),
                            context.getLeader().ip(), context.getLeader().port());
                    int leaderHealthCounter = context.healthCounter.incrementAndGet();
                    if (leaderHealthCounter > 3) {
                        startLeaderElection(context);
                        context.healthCounter.set(0);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
                throw new RuntimeException(e);
            }
        }, 0, 3, TimeUnit.SECONDS);
        LOG.info(context.id + " started health executor");
    }

    private void startLeaderElection(SystemContext context) {
        try {
            LOG.info(context.id + " starts leader election");

            if (context.getNeighbour() == null) {   // this client is the only one left in the system
                context.setLeader(new Leader(Node.getLocalIp(), context.listenPort));
                context.setLeaderContext(new LeaderContext());
                LOG.info(context.id + " assigns itself leader");
            } else {
                context.setElectionParticipant(true);
                ElectionMessage message = new ElectionMessage(new RemoteNode(Node.getLocalIp(), context.listenPort), false);

                client.unicast(new ElectionPayloadConverter().encode(Command.ELECTION, message),
                        context.getNeighbour().ip(), context.getNeighbour().port());
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
