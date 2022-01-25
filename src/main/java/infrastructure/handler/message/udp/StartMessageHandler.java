package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.LeaderInfoPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.converter.StartAckPayloadConverter;
import infrastructure.system.IPUtils;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.LeaderInfoMessage;
import infrastructure.system.message.StartAckMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Comparator;

import static infrastructure.system.IdService.nodeId;

public class StartMessageHandler implements UdpMessageHandler {

    private final static Logger LOG = LogManager.getLogger(StartMessageHandler.class);


    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<StartMessage> converter;

    public StartMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<StartMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            StartMessage startMessage = converter.decode(packet.getData());
            int port = startMessage.port();

            if (!context.id.equals(nodeId(packet.getAddress(), port))) {    // don't reply to own request

                client.unicast(
                        new StartAckPayloadConverter().encode(
                                Command.START_ACK, new StartAckMessage(context.getLeader())
                        ),
                        packet.getAddress(),
                        port
                );

                if (context.isLeader()) {
                    LOG.info("Add new Data-Node");

                    //TODO: Mode to "master-implementation"
                    context.addNode(new RemoteNode(packet.getAddress(), port));

                    if (context.getNodes().size() == 1) {
                        client.unicast(buildLeaderInfoMessage(context, context.getNodes().get(0)), context.getNodes().get(0).ip(), port);
                    }else{
                        for (int i = context.getNodes().size() -1; i >= 0 ; i--) {
                            if (i == context.getNodes().size() -1) {
                                client.unicast(buildLeaderInfoMessage(context, context.getNodes().get(0)), context.getNodes().get(i).ip(), port);
                            }else{
                                client.unicast(buildLeaderInfoMessage(context, context.getNodes().get(i+1)), context.getNodes().get(i).ip(), port);
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private byte[] buildLeaderInfoMessage(SystemContext context, RemoteNode neighbour){

        LeaderInfoMessage infoMessage = new LeaderInfoMessage(neighbour.ip(), neighbour.port());

        return new LeaderInfoPayloadConverter().encode(Command.LEADER_INFO, infoMessage);
    }
}
