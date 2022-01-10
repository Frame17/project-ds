import configuration.Configuration;
import infrastructure.Node;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        Node node = new Node(new Configuration());
        node.joinSystem();
    }
}
