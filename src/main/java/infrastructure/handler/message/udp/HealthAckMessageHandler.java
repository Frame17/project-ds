package infrastructure.handler.message.udp;

import infrastructure.system.SystemContext;

import java.net.DatagramPacket;

public class HealthAckMessageHandler implements UdpMessageHandler {

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        context.healthCounter.set(0);
    }
}
