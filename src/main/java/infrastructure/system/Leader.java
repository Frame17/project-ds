package infrastructure.system;

import java.net.InetAddress;

public record Leader(InetAddress ip, int port) {
}
