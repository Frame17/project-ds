package infrastructure.system;

import java.net.InetAddress;

public class IdService {

    public static String nodeId(InetAddress nodeIp, int nodePort) {
        return nodeIp.getHostAddress() + ':' + nodePort;
    }
}
