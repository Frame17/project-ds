package infrastructure.system.message;

import infrastructure.system.RemoteNode;

import java.util.List;

public record RecoveryMessage(RemoteNode node, List<String> fileChunks) {
}
