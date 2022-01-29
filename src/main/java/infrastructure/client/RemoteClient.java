package infrastructure.client;

import infrastructure.system.SystemContext;
import infrastructure.handler.request.RequestHandler;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

public interface RemoteClient<T> extends Closeable {

    void unicast(byte[] message, InetAddress ip, int port) throws IOException;

    void broadcast(byte[] message) throws IOException;

    void listen(SystemContext context, RequestHandler<T> requestHandler, int port);
}
