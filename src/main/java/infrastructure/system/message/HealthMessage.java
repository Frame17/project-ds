package infrastructure.system.message;

import infrastructure.system.RemoteNode;

public record HealthMessage(RemoteNode node) {
}
