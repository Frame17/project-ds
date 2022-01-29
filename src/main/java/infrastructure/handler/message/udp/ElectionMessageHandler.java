package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import infrastructure.system.message.ElectionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class ElectionMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(ElectionMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<ElectionMessage> converter;

    public ElectionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<ElectionMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        ElectionMessage electionMessage = converter.decode(packet.getData());
        LOG.info("Received election message from {} content {}", packet.getAddress().getHostAddress(), electionMessage);

        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            int compare = localIp.compareTo(electionMessage.candidate().getHostAddress());

            if (compare > 0) {
                client.unicast(converter.encode(Command.ELECTION, new ElectionMessage(InetAddress.getLocalHost(), false)),
                        context.getNeighbour().ip(), context.getNeighbour().port());
            } else if (compare == 0) {
                leaderSetup(context);
                client.unicast(converter.encode(Command.ELECTION, new ElectionMessage(InetAddress.getLocalHost(), true)),
                        context.getNeighbour().ip(), context.getNeighbour().port());
            } else {
                if (!context.isElectionParticipant()) {
                    client.unicast(converter.encode(Command.ELECTION, electionMessage),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                }
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    // todo - implement leader context setup
    private void leaderSetup(SystemContext context) throws UnknownHostException {
        context.setElectionParticipant(false);
        context.setLeader(new Leader(InetAddress.getLocalHost(), context.listenPort));
//        context.setLeaderContext();
    }
}
