package infrastructure.client;

import infrastructure.SystemContext;
import infrastructure.handler.request.RequestHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient implements RemoteClient<DatagramPacket> {

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
        DatagramPacket packet = new DatagramPacket(message, message.length);
        socket.send(packet);
        socket.close();
    }

    @Override
    public void listen(int port, SystemContext context, RequestHandler<DatagramPacket> requestHandler) {
        while (true) {
            try {
                DatagramSocket socket = new DatagramSocket(port);
                DatagramPacket packet = new DatagramPacket(new byte[1], 1);
                socket.receive(packet);
                requestHandler.handle(context, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
