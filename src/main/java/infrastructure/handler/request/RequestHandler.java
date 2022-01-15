package infrastructure.handler.request;

import infrastructure.system.SystemContext;

public interface RequestHandler<T> {
    void handle(SystemContext context, T request);
}
