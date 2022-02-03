package infrastructure.handler.message;

import infrastructure.system.SystemContext;

import java.net.Socket;

public interface ReliableMessageHandler<T> {
    void handle(SystemContext context, T message, Socket socket);
}
