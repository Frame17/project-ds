import configuration.Configuration;
import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.*;
import infrastructure.system.Leader;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileDeletionMessage;
import infrastructure.system.message.FileEditMessage;
import infrastructure.system.message.FileReadMessage;
import infrastructure.system.message.FileUploadMessage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
        FileUploadConverter converter = new FileUploadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage message = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), leader.ip(), leader.port() + 1);

        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(InetAddress.getLocalHost(), 4713))),
                leader.ip(), leader.port() + 1);

        DatagramSocket socket = new DatagramSocket(4713);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
        socket.receive(datagramPacket);
        FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
        System.out.println();
    }

    @Test
    void fileEditTest() throws IOException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
        FileUploadConverter uploadConverter = new FileUploadConverter();
        FileEditConverter editConverter = new FileEditConverter();
        FileUploadMessage uploadMessage = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        FileEditMessage editMessage = new FileEditMessage("abc", "adc".getBytes(StandardCharsets.UTF_8));
        client.unicast(uploadConverter.encode(Command.FILE_UPLOAD, uploadMessage), leader.ip(), leader.port() + 1);
        client.unicast(editConverter.encode(Command.FILE_EDIT, editMessage), leader.ip(), leader.port() + 1);

        // todo - read
    }

    @Test
    void fileDeleteTest() throws IOException, InterruptedException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
        FileUploadConverter converter = new FileUploadConverter();
        FileUploadMessage message = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), leader.ip(), leader.port() + 1);

        FileDeletionPayloadConverter deletionConverter = new FileDeletionPayloadConverter();
        FileDeletionMessage deletionMessage = new FileDeletionMessage("abc");
        client.unicast(deletionConverter.encode(Command.FILE_DELETE, deletionMessage), leader.ip(), leader.port() + 1);

        Thread.sleep(5000);

        // todo - read
    }


    private static int randomPort() {
        return random.nextInt(55_535) + 10_000;
    }
}
