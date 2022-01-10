package infrastructure.handler.message;

import infrastructure.SystemContext;

import java.net.DatagramPacket;

public interface MessageHandler {
    void handle(SystemContext context, DatagramPacket packet);
}
