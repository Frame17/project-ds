package configuration;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.client.TcpClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.*;
import infrastructure.handler.message.tcp.FileDeletionMessageHandler;
import infrastructure.handler.message.tcp.FileUploadMessageHandler;
import infrastructure.handler.message.tcp.TcpMessageHandler;
import infrastructure.handler.message.udp.*;
import infrastructure.handler.request.RequestHandler;
import infrastructure.handler.request.TcpRequestHandler;
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

    public RequestHandler<DatagramPacket> getDefaultClientRequestHandler() {
        return new UdpRequestHandler(udpMessageHandlers(getDefaultClient()));
    }

    private Map<Command, UdpMessageHandler> udpMessageHandlers(RemoteClient<DatagramPacket> client) {
        StartAckPayloadConverter startAckPayloadConverter = new StartAckPayloadConverter();
        HealthPayloadConverter healthPayloadConverter = new HealthPayloadConverter();
        NeighbourInfoPayloadConverter neighbourInfoPayloadConverter = new NeighbourInfoPayloadConverter();

        HashMap<Command, UdpMessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.START, new StartMessageHandler(client, new StartPayloadConverter(), startAckPayloadConverter, neighbourInfoPayloadConverter));
        messageHandlers.put(Command.START_ACK, new StartAckMessageHandler(client, startAckPayloadConverter, healthPayloadConverter));
        messageHandlers.put(Command.HEALTH, new HealthMessageHandler(client, healthPayloadConverter));
        messageHandlers.put(Command.HEALTH_ACK, new HealthAckMessageHandler());
        messageHandlers.put(Command.ELECTION, new ElectionMessageHandler(client, new ElectionPayloadConverter()));
        messageHandlers.put(Command.NEIGHBOUR_INFO, new NeighbourInfoMessageHandler(neighbourInfoPayloadConverter));

        return messageHandlers;
    }

    public RequestHandler<byte[]> getReliableClientRequestHandler() {
        return new TcpRequestHandler(tcpMessageHandlers(getReliableClient()));
    }

    private Map<Command, TcpMessageHandler> tcpMessageHandlers(RemoteClient<byte[]> client) {
        HashMap<Command, TcpMessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.FILE_UPLOAD, new FileUploadMessageHandler(client));
        messageHandlers.put(Command.FILE_DELETE, new FileDeletionMessageHandler(client, new FileDeletionPayloadConverter() ));
        return messageHandlers;
    }

    public RemoteClient<DatagramPacket> getDefaultClient() {
        return new UdpClient();
    }

    public RemoteClient<byte[]> getReliableClient() {
        return new TcpClient();
    }

    public SystemContext getContext() {
        try {
            return new SystemContext(nodeId(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT), DEFAULT_LISTEN_PORT);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}