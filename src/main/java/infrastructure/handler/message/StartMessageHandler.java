package infrastructure.handler.message;

import com.google.common.net.InetAddresses;
import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.LeaderInfoPayloadConverter;
import infrastructure.converter.PayloadConverter;
import infrastructure.converter.StartAckPayloadConverter;
import infrastructure.system.IPUtils;
import infrastructure.system.SystemContext;
import infrastructure.system.message.LeaderInfoMessage;
import infrastructure.system.message.StartAckMessage;
import infrastructure.system.message.StartMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import static infrastructure.system.IdService.nodeId;

public class StartMessageHandler implements MessageHandler {

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

                    //TODO: Refactor...
                    context.nodes.add(packet.getAddress());

                    context.nodes.sort(Comparator.comparing(IPUtils::getIntRepresentation));

                    LOG.info("New Ring {}", Arrays.toString(context.nodes.toArray()));

                    if (context.nodes.size() == 1) {
                        client.unicast(buildLeaderInfoMessage(context, context.nodes.get(0)), context.nodes.get(0), port);
                    }else{
                        for (int i = context.nodes.size() -1 ; i >= 0 ; i--) {
                            if (i == context.nodes.size() -1) {
                                client.unicast(buildLeaderInfoMessage(context, context.nodes.get(0)), context.nodes.get(i), port);
                            }else{
                                client.unicast(buildLeaderInfoMessage(context, context.nodes.get(i+1)), context.nodes.get(i), port);
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private byte[] buildLeaderInfoMessage(SystemContext context, InetAddress neighbour){

        LeaderInfoMessage infoMessage = new LeaderInfoMessage(neighbour);

        return new LeaderInfoPayloadConverter().encode(Command.LEADER_INFO, infoMessage);
    }
}
