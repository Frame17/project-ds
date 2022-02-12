package configuration;

import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.*;
import infrastructure.handler.message.tcp.FileDeletionMessageHandler;
import infrastructure.handler.message.tcp.FileEditMessageHandler;
import infrastructure.handler.message.tcp.FileReadMessageHandler;
import infrastructure.handler.message.tcp.FileUploadMessageHandler;
import infrastructure.handler.message.udp.*;
import infrastructure.handler.request.RequestHandler;
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
    private SystemContext context = null;
    public static final int DEFAULT_LISTEN_PORT = 4711;
    public static final int DEFAULT_FILES_LISTEN_PORT = 4712;
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
        RecoveryPayloadConverter recoveryConverter = new RecoveryPayloadConverter();

        HashMap<Command, UdpMessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.START, new StartMessageHandler(client, new StartPayloadConverter(), startAckPayloadConverter, neighbourInfoPayloadConverter));
        messageHandlers.put(Command.START_ACK, new StartAckMessageHandler(client, startAckPayloadConverter, healthPayloadConverter));
        messageHandlers.put(Command.HEALTH, new HealthMessageHandler(client, healthPayloadConverter));
        messageHandlers.put(Command.HEALTH_ACK, new HealthAckMessageHandler());
        messageHandlers.put(Command.ELECTION, new ElectionMessageHandler(client, new ElectionPayloadConverter(), recoveryConverter));
        messageHandlers.put(Command.NEIGHBOUR_INFO, new NeighbourInfoMessageHandler(neighbourInfoPayloadConverter));
        messageHandlers.put(Command.RECOVERY, new RecoveryMessageHandler(client, recoveryConverter));

        return messageHandlers;
    }

    public RequestHandler<DatagramPacket> getFileOperationsRequestHandler() {
        return new UdpRequestHandler(fileOperationsMessageHandlers(getReliableClient()));
    }

    private Map<Command, UdpMessageHandler> fileOperationsMessageHandlers(RemoteClient<DatagramPacket> client) {
        HashMap<Command, UdpMessageHandler> messageHandlers = new HashMap<>();
        messageHandlers.put(Command.FILE_UPLOAD, new FileUploadMessageHandler(client, new FileUploadConverter()));
        messageHandlers.put(Command.FILE_EDIT, new FileEditMessageHandler(client, new FileEditConverter()));
        messageHandlers.put(Command.FILE_DELETE, new FileDeletionMessageHandler(client, new FileDeletionPayloadConverter()));
        messageHandlers.put(Command.FILE_READ, new FileReadMessageHandler(client, new FileReadConverter()));
        return messageHandlers;
    }

    public RemoteClient<DatagramPacket> getDefaultClient() {
        return new UdpClient();
    }

    public RemoteClient<DatagramPacket> getReliableClient() {
        return new ReliableOrderedUdpClient(new ResendPayloadConverter(), getDefaultClient(), getContext());
    }

    public SystemContext getContext() {
        if (this.context == null) {
            context = new SystemContext(nodeId(Node.getLocalIp(), DEFAULT_LISTEN_PORT), DEFAULT_LISTEN_PORT, DEFAULT_FILES_LISTEN_PORT);
            if (defaultLeader) {
                context.setLeader(new Leader(Node.getLocalIp(), context.listenPort));
            }
        }
        return context;
    }
}