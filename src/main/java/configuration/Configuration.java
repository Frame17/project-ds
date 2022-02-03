package configuration;

import infrastructure.Command;
import infrastructure.client.ReliableRemoteClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.TcpClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.*;
import infrastructure.handler.message.tcp.FileReadMessageHandler;
import infrastructure.handler.message.tcp.FileUploadMessageHandler;
import infrastructure.handler.message.tcp.TcpMessageHandler;
import infrastructure.handler.message.udp.*;
import infrastructure.handler.request.ReliableRequestHandler;
import infrastructure.handler.request.RequestHandler;
import infrastructure.handler.request.TcpRequestHandler;
import infrastructure.handler.request.UdpRequestHandler;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static infrastructure.system.IdService.nodeId;

public class Configuration {
    public static final int DEFAULT_LISTEN_PORT = 4711;
    private boolean defaultLeader = false;

    public Configuration(String[] args) {
        for (String arg : args) {
            if ("LEADER".equals(arg) || "leader".equals(arg)) {
                this.defaultLeader = true;
            }
        }
    }


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

    public ReliableRequestHandler<byte[]> getReliableClientRequestHandler() {
        return new TcpRequestHandler(tcpMessageHandlers(getReliableClient()));
    }

    private Map<Command, TcpMessageHandler> tcpMessageHandlers(ReliableRemoteClient<byte[]> client) {
        HashMap<Command, TcpMessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.FILE_UPLOAD, new FileUploadMessageHandler(client));
        messageHandlers.put(Command.FILE_READ, new FileReadMessageHandler(client));
        return messageHandlers;
    }

    public RemoteClient<DatagramPacket> getDefaultClient() {
        return new UdpClient();
    }

    public ReliableRemoteClient<byte[]> getReliableClient() {
        return new TcpClient();
    }

    public SystemContext getContext() {
        try {
            SystemContext context = new SystemContext(nodeId(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT), DEFAULT_LISTEN_PORT);

            if (defaultLeader) {
                context.setLeader(new Leader(InetAddress.getLocalHost(), context.listenPort));
            }
            return context;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}