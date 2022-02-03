package infrastructure.client;

import infrastructure.handler.request.ReliableRequestHandler;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClient implements ReliableRemoteClient<byte[]> {
    private final static Logger LOG = LogManager.getLogger(TcpClient.class);
    private final ExecutorService listenExecutor = Executors.newSingleThreadExecutor();

    @Override
    public Socket unicast(byte[] message, InetAddress ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);

        OutputStream os = socket.getOutputStream();
        os.write(message);
        os.flush();


        return socket;
    }

    @Override
    public void listen(SystemContext context, ReliableRequestHandler<byte[]> requestHandler, int port) {
        listenExecutor.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port);) {
                LOG.info("Listening for tcp packets on {}", port);
                while (!Thread.currentThread().isInterrupted()) {

                    Socket clientSocket = serverSocket.accept();
                    Executors.newSingleThreadExecutor().execute(() -> {
                        BufferedInputStream in = null;
                        try{
                            // ReadAllBytes blocks until, inputstream is "closed", so we use the buffered one
                            in = new BufferedInputStream(clientSocket.getInputStream());

                            // Does't work
                            // byte[] bytes = in.readNBytes(in.available());

                            // Read Bytes from the BufferedInputStream until
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[4];
                            while ((nRead = in.readNBytes(data, 0, data.length)) != 0) {
                                buffer.write(data, 0, nRead);
                            }
                            buffer.flush();

                            byte[] bytes = buffer.toByteArray();

                            requestHandler.handle(context, bytes, clientSocket);

                        }catch (IOException e){
                            LOG.error(e);
                        }finally {
                            try {
                                in.close();
                                clientSocket.close();
                            } catch (IOException e) {
                                LOG.error(e);
                            }
                        }
                        LOG.info("Process finished {} at {}", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
                    });

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void close() {
        listenExecutor.shutdownNow();
    }
}
