package infrastructure.system.message;

import java.net.InetAddress;

public record FileReadDataMessage(String fileName, byte[] data, InetAddress ip, int port) {
}
