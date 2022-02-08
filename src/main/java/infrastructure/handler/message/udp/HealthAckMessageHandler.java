package infrastructure.handler.message.udp;

import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;

public class HealthAckMessageHandler implements UdpMessageHandler {

    @Override
    public void handle(SystemContext context, DatagramPacket packet, RemoteNode sender) {
        context.healthCounter.set(0);
    }
}
