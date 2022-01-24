import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.RemoteClient;
import infrastructure.system.Leader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class ApplicationTest {
    private static Leader leader;
    private static List<Node> system;
    private final Random random = new Random();

    @BeforeAll
    static void setUp() {
        try {
            leader = new Leader(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT);

            system = new ArrayList<>();
            Node node = new Node(new TestConfiguration(DEFAULT_LISTEN_PORT, leader));
            node.joinSystem();
            system.add(node);

            node = new Node(new TestConfiguration(11111, leader));
            node.joinSystem();
            system.add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void joinSystemTest() throws IOException {
        Node node = new Node(new TestConfiguration(randomPort(), null));
        node.joinSystem();

        await().atMost(5, TimeUnit.SECONDS).until(() -> leader.equals(node.context.getLeader()));
    }

    @Test
    public void healthcheckTest() throws IOException, InterruptedException {
        TestConfiguration configuration = new TestConfiguration(randomPort(), null);
        RemoteClient<DatagramPacket> remoteClient = configuration.getDefaultClient();

        Node node = new Node(configuration);
        node.joinSystem();

        Thread.sleep(5000);
        verify(remoteClient).unicast(eq(new byte[]{Command.HEALTH.command}), eq(leader.leaderIp()), eq(leader.leaderPort()));
    }

    @Test
    public void masterElectionTest() throws IOException {
        Node node = new Node(new TestConfiguration(randomPort(), null));
        node.joinSystem();
        system.add(node);

        system.get(0).startMasterElection();
        // todo - finalize election test
    }

    private int randomPort() {
        return random.nextInt(55_535) + 10_000;
    }
}
