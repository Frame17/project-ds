package infrastructure.system;

import java.net.InetAddress;

public record RemoteNode(InetAddress ip, int port) {
}
