import configuration.Configuration;
import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.FileEditConverter;
import infrastructure.converter.FileUploadConverter;
import infrastructure.converter.ResendPayloadConverter;
import infrastructure.system.Leader;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileEditMessage;
import infrastructure.system.message.FileUploadMessage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;

public class FeaturesTest {
    private static Leader leader;
    private static List<Node> system;
    private static final Random random = new Random();

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        try {
            leader = new Leader(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT);

            system = new ArrayList<>();
            Node node = new Node(new TestConfiguration(DEFAULT_LISTEN_PORT, leader, system));
            node.joinSystem();
            system.add(node);
            Thread.sleep(1000);

            node = new Node(new TestConfiguration(11111, null, system));
            node.joinSystem();
            system.add(node);
            Thread.sleep(1000);

            node = new Node(new TestConfiguration(randomPort(), null, system));
            node.joinSystem();
            system.add(node);
            Thread.sleep(1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void fileUploadTest() throws IOException, InterruptedException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration().getContext());
        FileUploadConverter converter = new FileUploadConverter();
        FileUploadMessage message = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), leader.ip(), leader.port() + 1);

        // todo - read
    }

    @Test
    void fileEditTest() throws IOException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration().getContext());
        FileUploadConverter uploadConverter = new FileUploadConverter();
        FileEditConverter editConverter = new FileEditConverter();
        FileUploadMessage uploadMessage = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        FileEditMessage editMessage = new FileEditMessage("abc", "adc".getBytes(StandardCharsets.UTF_8));
        client.unicast(uploadConverter.encode(Command.FILE_UPLOAD, uploadMessage), leader.ip(), leader.port() + 1);
        client.unicast(editConverter.encode(Command.FILE_EDIT, editMessage), leader.ip(), leader.port() + 1);

        // todo - read
    }

    private static int randomPort() {
        return random.nextInt(55_535) + 10_000;
    }
}