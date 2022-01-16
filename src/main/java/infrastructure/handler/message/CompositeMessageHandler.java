package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.system.SystemContext;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompositeMessageHandler implements MessageHandler {
    private final Map<Command, MessageHandler> messageHandlers;

    public CompositeMessageHandler(Map<Command, MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        var command = Arrays.stream(Command.values())
                .collect(Collectors.toMap(it -> it.command, Function.identity()))
                .get(packet.getData()[0]);

        messageHandlers
                .get(command)
                .handle(context, packet);
    }
}
