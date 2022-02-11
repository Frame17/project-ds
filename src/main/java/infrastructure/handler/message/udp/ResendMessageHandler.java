package infrastructure.handler.message.udp;

import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.Pair;
import infrastructure.system.ReliableClientContext;
import infrastructure.system.SystemContext;
import infrastructure.system.message.ResendMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Queue;

public class ResendMessageHandler implements UdpMessageHandler {
    private final RemoteClient<DatagramPacket> reliableClient;
    private final PayloadConverter<ResendMessage> converter;

    public ResendMessageHandler(RemoteClient<DatagramPacket> reliableClient, PayloadConverter<ResendMessage> converter) {
        this.reliableClient = reliableClient;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket message) {
        ResendMessage resendMessage = converter.decode(message.getData());
        ReliableClientContext reliableClientContext = context.getReliableClientContext();
        
        try {
            Queue<Pair<Integer, byte[]>> holdBackQueue = reliableClientContext.previousMessages.get(resendMessage.ip());
            Pair<Integer, byte[]> packet = holdBackQueue.peek();
            while (packet.first() < resendMessage.id()) {
                holdBackQueue.poll();
                packet = holdBackQueue.peek();
            }

            reliableClient.unicast(packet.second(), resendMessage.ip().ip(), resendMessage.ip().port());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}