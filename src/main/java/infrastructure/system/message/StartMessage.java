package infrastructure.system.message;

import java.net.InetAddress;

public record StartMessage(InetAddress ip, int port) {
}
