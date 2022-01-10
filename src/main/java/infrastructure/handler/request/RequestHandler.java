package infrastructure.handler.request;

import infrastructure.Node;
import infrastructure.SystemContext;

public interface RequestHandler<T> {
    void handle(SystemContext context, T request);
}
