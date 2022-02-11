package infrastructure.system.message;

import infrastructure.system.RemoteNode;

public record FileReadMessage(String fileName, byte[] file, RemoteNode client) {
}