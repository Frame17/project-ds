package infrastructure.system.message;

import java.net.InetAddress;

public record FileReadMessage(String fileName, InetAddress ip, int port) {
}
