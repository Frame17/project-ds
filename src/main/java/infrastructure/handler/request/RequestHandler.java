package infrastructure.handler.request;

import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;

public interface RequestHandler<T> {
    void handle(SystemContext context, T request, RemoteNode sender);
}
