package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.*;
import infrastructure.system.message.ElectionMessage;
import infrastructure.system.message.RecoveryMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ElectionMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(ElectionMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<ElectionMessage> electionConverter;
    private final PayloadConverter<RecoveryMessage> recoveryConverter;

    public ElectionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<ElectionMessage> electionConverter, PayloadConverter<RecoveryMessage> recoveryConverter) {
        this.client = client;
        this.electionConverter = electionConverter;
        this.recoveryConverter = recoveryConverter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        ElectionMessage electionMessage = electionConverter.decode(packet.getData());
        LOG.info(context.id + " receives election message from {} content {}", packet.getAddress().getHostAddress(), electionMessage);

        try {
            int compare = context.id.compareTo(IdService.nodeId(electionMessage.candidate().ip(), electionMessage.candidate().port()));

            if (compare > 0) {
                if (!context.isElectionParticipant()) {
                    client.unicast(electionConverter.encode(Command.ELECTION, new ElectionMessage(new RemoteNode(Node.getLocalIp(), context.listenPort), false)),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(true);
                }
            } else if (compare == 0) {
                if (!electionMessage.isLeader()) {
                    leaderSetup(context);
                    client.unicast(electionConverter.encode(Command.ELECTION, new ElectionMessage(electionMessage.candidate(), true)),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(false);
                }
            } else {
                if (electionMessage.isLeader()) {
                    context.setLeader(new Leader(electionMessage.candidate().ip(), electionMessage.candidate().port()));
                    context.healthCounter.set(0);
                } else {
                    client.unicast(electionConverter.encode(Command.ELECTION, electionMessage),
                            context.getNeighbour().ip(), context.getNeighbour().port());
                    context.setElectionParticipant(true);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void leaderSetup(SystemContext context) throws UnknownHostException {
        context.setElectionParticipant(false);
        context.setLeader(new Leader(Node.getLocalIp(), context.listenPort));
        context.getSelf().setupLeader(context);
        HashMap<String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;

        File files = new File("files/");
        RemoteNode current = new RemoteNode(InetAddress.getLocalHost(), context.listenPort);
        if (files.exists() && files.listFiles() != null) {
            Arrays.stream(files.listFiles())
                    .map(File::getName)
                    .forEach(chunk -> {
                        String fileName = chunk.substring(0, chunk.lastIndexOf('-'));
                        if (chunksDistributionTable.containsKey(fileName)) {
                            chunksDistributionTable.get(fileName).add(new FileChunk(chunk, current));
                        } else {
                            ArrayList<FileChunk> fileChunks = new ArrayList<>();
                            fileChunks.add(new FileChunk(chunk, current));
                            chunksDistributionTable.put(fileName, fileChunks);
                        }
                    });
        }

        try {
            client.broadcast(recoveryConverter.encode(Command.RECOVERY, new RecoveryMessage(current, null)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
