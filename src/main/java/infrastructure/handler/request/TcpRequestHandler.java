package infrastructure.handler.request;

import infrastructure.Command;
import infrastructure.handler.message.tcp.TcpMessageHandler;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TcpRequestHandler implements RequestHandler<byte[]> {
    private final Map<Command, TcpMessageHandler> messageHandlers;

    public TcpRequestHandler(Map<Command, TcpMessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
    }

    @Override
    public void handle(SystemContext context, byte[] request, RemoteNode sender) {
        var command = Arrays.stream(Command.values())
                .collect(Collectors.toMap(it -> it.command, Function.identity()))
                .get(request[0]);
        
        messageHandlers.get(command).handle(context, request, sender);
    }
}
