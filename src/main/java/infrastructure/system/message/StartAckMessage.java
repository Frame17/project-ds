package infrastructure.system.message;

import infrastructure.system.Leader;
import infrastructure.system.RemoteNode;

public record StartAckMessage(Leader leader, RemoteNode neighbour) {
}
