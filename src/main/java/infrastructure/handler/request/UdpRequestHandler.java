package infrastructure.handler.request;

import infrastructure.Command;
import infrastructure.handler.message.udp.UdpMessageHandler;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UdpRequestHandler implements RequestHandler<DatagramPacket> {
    private final Map<Command, UdpMessageHandler> messageHandlers;

    public UdpRequestHandler(Map<Command, UdpMessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
    }

    public void handle(SystemContext context, DatagramPacket packet) {
        var command = Arrays.stream(Command.values())
                .collect(Collectors.toMap(it -> it.command, Function.identity()))
                .get(packet.getData()[0]);

        messageHandlers.get(command).handle(context, packet);
    }
}
