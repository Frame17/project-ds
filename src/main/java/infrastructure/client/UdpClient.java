package infrastructure.client;

import infrastructure.system.SystemContext;
import infrastructure.handler.request.RequestHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class UdpClient implements RemoteClient<DatagramPacket> {
    private final ExecutorService listenExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void unicast(byte[] message, InetAddress ip, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);
        socket.send(packet);
        socket.close();
    }

    @Override
    public void broadcast(byte[] message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getByName("255.255.255.255"), DEFAULT_LISTEN_PORT);
        socket.send(packet);
        socket.close();
    }

    @Override
    public void listen(SystemContext context, RequestHandler<DatagramPacket> requestHandler, int port) {
        listenExecutor.execute(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(port);
                while (true) {
                    DatagramPacket packet = new DatagramPacket(new byte[7], 7);
                    socket.receive(packet);
                    requestHandler.handle(context, packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
