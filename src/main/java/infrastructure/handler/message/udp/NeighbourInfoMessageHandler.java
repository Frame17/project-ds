package infrastructure.handler.message.udp;

import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.NeighbourInfoMessage;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;

public class NeighbourInfoMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(NeighbourInfoMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<NeighbourInfoMessage> converter;

    public NeighbourInfoMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<NeighbourInfoMessage> converter){
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        NeighbourInfoMessage infoMessage = converter.decode(packet.getData());

        RemoteNode remoteNode = new RemoteNode(infoMessage.neighbour(), infoMessage.port());
        context.setNeighbour(remoteNode);

        LOG.info("Set new neighbour {}", context.getNeighbour());
    }
}
