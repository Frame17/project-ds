package infrastructure.client;

import infrastructure.handler.request.RequestHandler;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClient implements RemoteClient<byte[]> {
    private final static Logger LOG = LogManager.getLogger(TcpClient.class);
    private final ExecutorService listenExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void unicast(byte[] message, InetAddress ip, int port) throws IOException {
        try (Socket socket = new Socket(ip, port)) {
            OutputStream os = socket.getOutputStream();
            os.write(message);
            os.flush();
        }
    }

    @Override
    public void broadcast(byte[] message) {

    }

    @Override
    public void listen(SystemContext context, RequestHandler<byte[]> requestHandler, int port) {
        listenExecutor.execute(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                LOG.info("Listening for tcp packets on {}", port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    InputStream in = clientSocket.getInputStream();
                    requestHandler.handle(context, in.readAllBytes());
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
