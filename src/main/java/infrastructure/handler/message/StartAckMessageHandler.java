package infrastructure.handler.message;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartAckMessageHandler implements MessageHandler {
    private final RemoteClient<DatagramPacket> client;

    public StartAckMessageHandler(RemoteClient<DatagramPacket> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        context.setLeader(extractLeader(packet.getData()));
        startHealthCheck(context);
    }

    // todo - move to Payload Converter
    private Leader extractLeader(byte[] message) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
            short leaderPort = buffer.getShort();
            byte[] leaderIp = new byte[4 * Byte.BYTES];
            buffer.get(leaderIp);
            InetAddress leader = InetAddress.getByAddress(leaderIp);

            return new Leader(leader, leaderPort);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void startHealthCheck(SystemContext context) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.schedule(() -> {
            try {
                client.unicast(new byte[]{Command.HEALTH.command}, context.getLeader().leaderIp(), context.getLeader().leaderPort());
                context.healthCounter.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
