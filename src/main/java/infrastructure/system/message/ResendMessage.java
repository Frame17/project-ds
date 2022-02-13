package infrastructure.system.message;

import infrastructure.system.RemoteNode;

public record ResendMessage(RemoteNode node, int id) {
}
