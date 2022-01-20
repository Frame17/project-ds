package infrastructure.system;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class RemoteNode {

    private final InetAddress inetAddress;
    private final int port;
    private Date lastHealthCheck;
    private final SystemContext context;

    public RemoteNode(InetAddress inetAddress, int port, Date lastHealthCheck, SystemContext context) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.lastHealthCheck = lastHealthCheck;
        this.context = context;
    }

    public Socket openDirectChannel(){
        throw new IllegalStateException("Not implemented");
    }


    public InetAddress getInetAddress() {
        return inetAddress;
    }


    public int getPort() {
        return port;
    }

    public Date getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(Date lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    @Override
    public String toString() {
        return "RemoteNode{" +
                "inetAddress=" + inetAddress.getHostAddress() +
                ", port=" + port +
                '}';
    }
}
