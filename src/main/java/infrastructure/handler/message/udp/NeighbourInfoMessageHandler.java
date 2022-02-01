package infrastructure.handler.message.udp;

import infrastructure.converter.PayloadConverter;
import infrastructure.system.SystemContext;
import infrastructure.system.message.NeighbourInfoMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;

public class NeighbourInfoMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(NeighbourInfoMessageHandler.class);
    private final PayloadConverter<NeighbourInfoMessage> converter;

    public NeighbourInfoMessageHandler(PayloadConverter<NeighbourInfoMessage> converter){
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        NeighbourInfoMessage message = converter.decode(packet.getData());

        LOG.info(context.id + " sets neighbour {}", message.neighbour());
        context.setNeighbour(message.neighbour());
    }
}
