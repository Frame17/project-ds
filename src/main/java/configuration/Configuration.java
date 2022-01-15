package configuration;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.handler.message.*;
import infrastructure.handler.request.RequestHandler;
import infrastructure.handler.request.UdpRequestHandler;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    public RequestHandler<DatagramPacket> getRequestHandler() {
        return new UdpRequestHandler(new CompositeMessageHandler(messageHandlers(getRemoteClient())));
    }

    private Map<Command, MessageHandler> messageHandlers(RemoteClient<DatagramPacket> client) {
        HashMap<Command, MessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.START, new StartMessageHandler(client));
        messageHandlers.put(Command.START_ACK, new StartAckMessageHandler(client));
        messageHandlers.put(Command.HEALTH_ACK, new HealthAckMessageHandler());
        return messageHandlers;
    }

    public RemoteClient<DatagramPacket> getRemoteClient() {
        return new UdpClient();
    }
}