package infrastructure.system.message;

import java.net.InetAddress;

public record NeighbourInfoMessage(InetAddress neighbour, int port) {
}
