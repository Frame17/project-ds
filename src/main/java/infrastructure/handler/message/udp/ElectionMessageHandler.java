package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.message.ElectionMessage;
import infrastructure.system.IPUtils;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class ElectionMessageHandler implements UdpMessageHandler {

    private final static Logger LOG = LogManager.getLogger(ElectionMessageHandler.class);

    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<ElectionMessage> converter;

    public ElectionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<ElectionMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    private boolean participant;

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        ElectionMessage electionMassage = converter.decode(packet.getData());

        // LOG.info("Received election message from {} content {}", packet.getAddress().getHostAddress(), electionMassage);



        try {
            int local_int = IPUtils.getIntRepresentation(context.getLocalAddress());
            int mid_int = IPUtils.getIntRepresentation(electionMassage.mid());

            LOG.info("local ip {}({}), mid {}({}), participant {}", context.getLocalAddress(), local_int,electionMassage.mid(), mid_int, participant );

            //
            if (electionMassage.isLeader() && local_int != mid_int) {

                client.unicast(
                        converter.encode(Command.ELECTION, electionMassage),
                        context.getNeighbour().ip(),
                        context.getNeighbour().port());
                participant = false;

                // Not elected node
                context.setLeader(new Leader(electionMassage.mid(), DEFAULT_LISTEN_PORT));

            }else if (electionMassage.isLeader()){
                // Elec
                context.setLeader(new Leader(electionMassage.mid(), DEFAULT_LISTEN_PORT));
                context.actAsLeader();
            }else {


                if (mid_int > local_int) {
                    participant = true;

                    client.unicast(
                            converter.encode(Command.ELECTION, electionMassage),
                            context.getNeighbour().ip(),
                            context.getNeighbour().port());
                } else if (mid_int < local_int && !participant) {
                    participant = true;
                    ElectionMessage newElectionMassage = new ElectionMessage(context.getLocalAddress(), false);

                    client.unicast(
                            converter.encode(Command.ELECTION, newElectionMassage),
                            context.getNeighbour().ip(),
                            context.getNeighbour().port());
                } else if (mid_int < local_int && participant) {
                   // This can happen when two nodes have started the election, that's fine.
                } else if (local_int == mid_int) {
                    participant = false;

                    ElectionMessage newElectionMassage = new ElectionMessage(context.getLocalAddress(), true);
                    client.unicast(
                            converter.encode(Command.ELECTION, newElectionMassage),
                            context.getNeighbour().ip(),
                            context.getNeighbour().port());
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
