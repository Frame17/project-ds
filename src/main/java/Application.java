import configuration.Configuration;
import infrastructure.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Application {

    private final static Logger LOG = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        LOG.info("Starting client");
        Node node = new Node(new Configuration(args));
        node.joinSystem();
    }
}
