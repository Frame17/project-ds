package infrastructure.client;

import infrastructure.handler.request.RequestHandler;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class UdpClient implements RemoteClient<DatagramPacket> {
    private final static Logger LOG = LogManager.getLogger(UdpClient.class);
    private final ExecutorService listenExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void unicast(byte[] message, InetAddress ip, int port) throws IOException {
        if (!closed.get()) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);
            socket.send(packet);
            socket.close();
        }
    }

    @Override
    public void broadcast(byte[] message) throws IOException {
        if (!closed.get()) {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getByName("255.255.255.255"), DEFAULT_LISTEN_PORT);
            LOG.info("Send broadcast to {}", packet.getPort());

            socket.send(packet);
            socket.close();
        }
    }

    @Override
    public void listen(SystemContext context, RequestHandler<DatagramPacket> requestHandler, int port) {
        listenExecutor.execute(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                LOG.info("Listening for udp packets on {}", port);
                while (!Thread.currentThread().isInterrupted()) {
                    int size = 1024;
                    DatagramPacket packet = new DatagramPacket(new byte[size], size);
                    socket.receive(packet);
                    requestHandler.handle(context, packet);
                }
                socket.disconnect();
            } catch (IOException e) {
                LOG.error(e);
            }
        });
    }

    @Override
    public void close() {
        listenExecutor.shutdownNow();
        closed.set(true);
    }
}
