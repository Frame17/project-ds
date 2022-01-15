package infrastructure.handler.message;

import infrastructure.system.SystemContext;

import java.net.DatagramPacket;

public interface MessageHandler {
    void handle(SystemContext context, DatagramPacket packet);
}
