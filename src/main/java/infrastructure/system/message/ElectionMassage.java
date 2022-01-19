package infrastructure.system.message;

import java.net.InetAddress;

public record ElectionMassage(InetAddress mid, boolean isLeader) {

}
