package infrastructure.system.message;

import java.net.InetAddress;

public record ElectionMessage(InetAddress mid, boolean isLeader) {

}
