package infrastructure.handler.message;

import infrastructure.system.SystemContext;

public interface MessageHandler<T> {
    void handle(SystemContext context, T message);
}
