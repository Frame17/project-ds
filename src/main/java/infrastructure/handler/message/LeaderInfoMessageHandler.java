package infrastructure.handler.message;

import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
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

        context.neighbour = infoMessage.neighbour();
        LOG.info("Set new neighbour {}", context.neighbour.getHostAddress());
    }
}
