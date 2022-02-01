package infrastructure.system.message;

import infrastructure.system.RemoteNode;

public record ElectionMessage(RemoteNode candidate, boolean isLeader) {

}
