package configuration;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.StartAckPayloadConverter;
import infrastructure.converter.StartPayloadConverter;
import infrastructure.handler.message.*;
import infrastructure.handler.request.RequestHandler;
import infrastructure.handler.request.UdpRequestHandler;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static infrastructure.system.IdService.nodeId;

public class Configuration {
    public static final int DEFAULT_LISTEN_PORT = 4711;

    public RequestHandler<DatagramPacket> getRequestHandler() {
        return new UdpRequestHandler(new CompositeMessageHandler(messageHandlers(getRemoteClient())));
    }

    private Map<Command, MessageHandler> messageHandlers(RemoteClient<DatagramPacket> client) {
        HashMap<Command, MessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.START, new StartMessageHandler(client, new StartPayloadConverter()));
        messageHandlers.put(Command.START_ACK, new StartAckMessageHandler(client, new StartAckPayloadConverter()));
        messageHandlers.put(Command.HEALTH, new HealthMessageHandler(client, null));
        messageHandlers.put(Command.HEALTH_ACK, new HealthAckMessageHandler());
        return messageHandlers;
    }

    public RemoteClient<DatagramPacket> getRemoteClient() {
        return new UdpClient();
    }

    public SystemContext getContext() {
        try {
            return new SystemContext(nodeId(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT), DEFAULT_LISTEN_PORT);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}