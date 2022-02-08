package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.*;
import infrastructure.system.message.ElectionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElectionMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(ElectionMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<ElectionMessage> converter;

    public ElectionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<ElectionMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet, RemoteNode sender) {
        ElectionMessage electionMessage = converter.decode(packet.getData());
        LOG.info(context.id + " receives election message from {} content {}", packet.getAddress().getHostAddress(), electionMessage);

        try {
            int compare = context.id.compareTo(IdService.nodeId(electionMessage.candidate().ip(), electionMessage.candidate().port()));

            if (compare > 0) {
                if (!context.isElectionParticipant()) {
                    client.unicast(converter.encode(Command.ELECTION, new ElectionMessage(new RemoteNode(InetAddress.getLocalHost(), context.listenPort), false)),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(true);
                }
            } else if (compare == 0) {
                if (!electionMessage.isLeader()) {
                    leaderSetup(context);
                    client.unicast(converter.encode(Command.ELECTION, new ElectionMessage(electionMessage.candidate(), true)),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(false);
                }
            } else {
                if (electionMessage.isLeader()) {
                    context.setLeader(new Leader(electionMessage.candidate().ip(), electionMessage.candidate().port()));
                } else {
                    client.unicast(converter.encode(Command.ELECTION, electionMessage),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(true);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    // todo - implement leader context recovery
    private void leaderSetup(SystemContext context) throws UnknownHostException {
        context.setElectionParticipant(false);
        context.setLeader(new Leader(InetAddress.getLocalHost(), context.listenPort));
        context.setLeaderContext(new LeaderContext());
    }
}
