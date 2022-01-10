package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.SystemContext;
import infrastructure.client.RemoteClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static infrastructure.Node.LISTEN_PORT;

public class StartAckMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        try {
            byte[] bytes = Arrays.copyOfRange(packet.getData(), 1, packet.getData().length);
            InetAddress leader = InetAddress.getByAddress(bytes);
            context.setLeader(leader);
            startHealthCheck(context);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void startHealthCheck(SystemContext context) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.schedule(() -> {
            try {
                client.unicast(new byte[]{Command.HEALTH.command}, context.getLeader(), LISTEN_PORT);
                context.healthCounter.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
