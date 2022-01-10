package configuration;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.handler.message.CompositeMessageHandler;
import infrastructure.handler.message.MessageHandler;
import infrastructure.handler.request.RequestHandler;
import infrastructure.handler.request.UdpRequestHandler;

import java.net.DatagramPacket;
import java.util.Collections;
import java.util.Map;

public class Configuration {

    public RequestHandler<DatagramPacket> getRequestHandler() {
        return new UdpRequestHandler(new CompositeMessageHandler(messageHandlers()));
    }

    private Map<Command, MessageHandler> messageHandlers() {
        return Collections.emptyMap();
    }

    public RemoteClient<DatagramPacket> getRemoteClient() {
        return new UdpClient();
    }
}