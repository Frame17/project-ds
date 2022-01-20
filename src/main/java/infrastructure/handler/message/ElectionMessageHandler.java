package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.message.ElectionMassage;
import infrastructure.system.IPUtils;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class ElectionMessageHandler implements MessageHandler{

    private final static Logger LOG = LogManager.getLogger(ElectionMessageHandler.class);

    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<ElectionMassage> converter;

    public ElectionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<ElectionMassage> converter) {
        this.client = client;
        this.converter = converter;
    }

    private boolean participant;
    private InetAddress leader_mid;

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        ElectionMassage electionMassage = converter.convert(packet.getData());

        // LOG.info("Received election message from {} content {}", packet.getAddress().getHostAddress(), electionMassage);



        try {
            int local_int = IPUtils.getIntRepresentation(context.getLocalAddress());
            int mid_int = IPUtils.getIntRepresentation(electionMassage.mid());

            LOG.info("local ip {}({}), mid {}({}), participant {}", context.getLocalAddress(), local_int,electionMassage.mid(), mid_int, participant );

            //
            if (electionMassage.isLeader() && local_int != mid_int) {

                client.unicast(converter.encode(Command.ELECTION, electionMassage), context.getNeighbour().getInetAddress(), DEFAULT_LISTEN_PORT);
                participant = false;

                // Not elected node
                leader_mid = electionMassage.mid();
                context.setLeader(new Leader(electionMassage.mid(), DEFAULT_LISTEN_PORT));

            }else if (electionMassage.isLeader()){
                // Elec
                context.setLeader(new Leader(electionMassage.mid(), DEFAULT_LISTEN_PORT));
                context.actAsLeader();
            }else {


                if (mid_int > local_int) {
                    participant = true;

                    client.unicast(converter.encode(Command.ELECTION, electionMassage), context.getNeighbour().getInetAddress(), DEFAULT_LISTEN_PORT);
                } else if (mid_int < local_int && !participant) {
                    participant = true;
                    ElectionMassage newElectionMassage = new ElectionMassage(context.getLocalAddress(), false);

                    client.unicast(converter.encode(Command.ELECTION, newElectionMassage), context.getNeighbour().getInetAddress(), DEFAULT_LISTEN_PORT);
                } else if (mid_int < local_int && participant) {
                    LOG.error("Election error state (╯°□°)╯︵ ┻━┻");
                } else if (local_int == mid_int) {
                    participant = false;

                    ElectionMassage newElectionMassage = new ElectionMassage(context.getLocalAddress(), true);
                    client.unicast(converter.encode(Command.ELECTION, newElectionMassage), context.getNeighbour().getInetAddress(), DEFAULT_LISTEN_PORT);
                }
            }
            /*
            if (electionMassage.isLeader()){
                leader_mid = electionMassage.mid();
                participant = false;

                client.unicast(buildMessage(context, electionMassage), context.neighbour, DEFAULT_LISTEN_PORT);

                return;
            }

            if (mid_int < local_int && !participant) {
                ElectionMassage newElectionMassage = new ElectionMassage(context.getLocalAddress(), false);
                participant = true;

                client.unicast(buildMessage(context, newElectionMassage), context.neighbour, DEFAULT_LISTEN_PORT);
            }else if (mid_int > local_int){
                participant = true;

                client.unicast(buildMessage(context, electionMassage), context.neighbour, DEFAULT_LISTEN_PORT);
            }else if (mid_int == local_int){
                leader_mid = electionMassage.mid();
                ElectionMassage newElectionMassage = new ElectionMassage(electionMassage.mid(), true);
                participant = false;

                client.unicast(buildMessage(context, newElectionMassage), context.neighbour, DEFAULT_LISTEN_PORT);
            }
            */


        } catch (IOException e) {
            LOG.error(e);
        }


    }
}
