package infrastructure.handler.message;

import infrastructure.Command;

public record Message<T>(Command command, T payload) {}
