package infrastructure.handler.message;

import infrastructure.system.SystemContext;

import java.net.DatagramPacket;

public class HealthAckMessageHandler implements MessageHandler {

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        context.healthCounter.set(0);
    }
}
