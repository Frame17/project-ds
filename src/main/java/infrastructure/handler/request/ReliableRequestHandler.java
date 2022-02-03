package infrastructure.handler.request;

import infrastructure.system.SystemContext;

import java.net.Socket;

public interface ReliableRequestHandler<T> {
    void handle(SystemContext context, T request, Socket socket);
}
