package infrastructure;

public enum Command {
    START(0), START_ACK(1), HEALTH_ACK(2), HEALTH(3);

    public final byte command;

    Command(int command) {
        this.command = ((byte) command);
    }
}
