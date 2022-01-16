import infrastructure.Node;
import infrastructure.system.Leader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static configuration.Configuration.DEFAULT_LISTEN_PORT;
import static org.awaitility.Awaitility.await;

public class ApplicationTest {
    private Leader leader;
    private List<Node> system;

    @BeforeEach
    void setUp() {
        try {
            leader = new Leader(InetAddress.getLocalHost(), DEFAULT_LISTEN_PORT);

            system = new ArrayList<>();
            Node node = new Node(new TestConfiguration(DEFAULT_LISTEN_PORT, leader));
            node.joinSystem();
            system.add(node);

            node = new Node(new TestConfiguration((short) 11111, leader));
            node.joinSystem();
            system.add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void joinSystemTest() throws IOException {
        Node node = new Node(new TestConfiguration((short) 22222, null));
        node.joinSystem();

        await().atMost(5, TimeUnit.SECONDS).until(() -> leader.equals(node.context.getLeader()));
        System.out.println();
    }
}
