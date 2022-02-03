package infrastructure.client;

import infrastructure.handler.request.ReliableRequestHandler;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.SystemContext;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public interface ReliableRemoteClient<T> extends Closeable {

    Socket unicast(byte[] message, InetAddress ip, int port) throws IOException;

    void listen(SystemContext context, ReliableRequestHandler<T> requestHandler, int port);

}
