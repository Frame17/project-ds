package infrastructure.system;

import java.net.InetAddress;

public record Leader(InetAddress leaderIp, int leaderPort) {
    @Override
    public String toString() {
        return "Leader{" +
                "leaderIp=" + leaderIp.getHostAddress() +
                ", leaderPort=" + leaderPort +
                '}';
    }
}
