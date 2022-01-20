package infrastructure.system.message;

import java.net.InetAddress;

public record LeaderInfoMessage (InetAddress neighbour, int port){
}
