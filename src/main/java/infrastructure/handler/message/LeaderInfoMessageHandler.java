package infrastructure.handler.message;

import configuration.Configuration;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.LeaderInfoMessage;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;

public class LeaderInfoMessageHandler implements MessageHandler{

    private final static Logger LOG = LogManager.getLogger(LeaderInfoMessageHandler.class);

    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<LeaderInfoMessage> converter;

    public LeaderInfoMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<LeaderInfoMessage> converter){
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        LeaderInfoMessage infoMessage = converter.decode(packet.getData());

        RemoteNode remoteNode = new RemoteNode(infoMessage.neighbour(), infoMessage.port(), null, context);
        context.setNeighbour(remoteNode);

        LOG.info("Set new neighbour {}", context.getNeighbour().getInetAddress().getHostAddress());
    }
}
