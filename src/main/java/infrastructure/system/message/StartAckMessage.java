package infrastructure.system.message;

import infrastructure.system.Leader;

public record StartAckMessage(Leader leader) {
}
