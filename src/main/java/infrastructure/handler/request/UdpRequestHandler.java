package infrastructure.handler.request;

import infrastructure.SystemContext;
import infrastructure.handler.message.MessageHandler;

import java.net.DatagramPacket;

public class UdpRequestHandler implements RequestHandler<DatagramPacket> {
    private final MessageHandler messageHandler;

    public UdpRequestHandler(MessageHandler messageHandlers) {
        this.messageHandler = messageHandlers;
    }

    public void handle(SystemContext context, DatagramPacket packet) {
        messageHandler.handle(context, packet);
    }
}
