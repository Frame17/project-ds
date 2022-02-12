package infrastructure.client;

import infrastructure.Command;
import infrastructure.Node;
import infrastructure.converter.PayloadConverter;
import infrastructure.handler.request.RequestHandler;
import infrastructure.system.Pair;
import infrastructure.system.ReliableClientContext;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.ResendMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static infrastructure.client.UdpClient.PACKET_SIZE;

public class ReliableOrderedUdpClient implements RemoteClient<DatagramPacket> {
    private final static Logger LOG = LogManager.getLogger(ReliableOrderedUdpClient.class);
    private final ExecutorService listenExecutor = Executors.newSingleThreadExecutor();
    private final PayloadConverter<ResendMessage> converter;
    private final RemoteClient<DatagramPacket> baseClient;
    private final SystemContext context;

    public ReliableOrderedUdpClient(PayloadConverter<ResendMessage> converter, RemoteClient<DatagramPacket> baseClient, SystemContext context) {
        this.converter = converter;
        this.baseClient = baseClient;
        this.context = context;
    }

    @Override
    public void unicast(byte[] message, InetAddress ip, int port) throws IOException {
        ReliableClientContext reliableClientContext = context.getReliableClientContext();
        RemoteNode target = new RemoteNode(ip, port);
        int sequenceCounter = reliableClientContext.sendSequences.getOrDefault(target, 0);

        if (!reliableClientContext.previousMessages.containsKey(target)) {
            Queue<Pair<Integer, byte[]>> queue = new ArrayDeque<>();
            queue.add(new Pair<>(sequenceCounter, message));
            reliableClientContext.previousMessages.put(target, queue);
        } else {
            reliableClientContext.previousMessages.get(target).add(new Pair<>(sequenceCounter, message));
        }

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + message.length);
        buffer.putInt(sequenceCounter);
        buffer.put(message);
        baseClient.unicast(buffer.array(), ip, port);
        reliableClientContext.sendSequences.put(target, sequenceCounter + 1);
    }

    @Override
    public void broadcast(byte[] message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void listen(SystemContext context, RequestHandler<DatagramPacket> requestHandler, int port) {
        ReliableClientContext reliableClientContext = context.getReliableClientContext();

        listenExecutor.execute(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                LOG.info("Reliable ordered client listening on {}", port);
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                    socket.receive(packet);
                    if (deliver(context, requestHandler, packet)) {
                        reliableClientContext.holdBackQueue.forEach(pending -> {
                            try {
                                deliver(context, requestHandler, pending.second());
                            } catch (IOException e) {
                                LOG.error(e);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        });
    }

    private boolean deliver(SystemContext context, RequestHandler<DatagramPacket> requestHandler, DatagramPacket packet) throws IOException {
        ReliableClientContext reliableClientContext = context.getReliableClientContext();

        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
        int sequenceNumber = buffer.getInt();
        buffer.get();
        RemoteNode sender = new RemoteNode(packet.getAddress(), 0);
        Integer receivedSequenceNumber = reliableClientContext.receiveSequences.getOrDefault(sender, 0);

        if (sequenceNumber == receivedSequenceNumber) {
            byte[] array = Arrays.copyOfRange(packet.getData(), 4, PACKET_SIZE);
            requestHandler.handle(context, new DatagramPacket(array, array.length, packet.getAddress(), packet.getPort()));
            reliableClientContext.receiveSequences.put(sender, receivedSequenceNumber + 1);
        } else if (sequenceNumber > receivedSequenceNumber) {
            reliableClientContext.holdBackQueue.add(new Pair<>(sequenceNumber, packet));

            ResendMessage resendMessage = new ResendMessage(new RemoteNode(Node.getLocalIp(), context.listenPort), receivedSequenceNumber);
            baseClient.unicast(converter.encode(Command.RESEND, resendMessage), sender.ip(), sender.port());
            return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {

    }
}
