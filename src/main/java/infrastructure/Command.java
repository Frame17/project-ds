package infrastructure;

public enum Command {
    START(0), START_ACK(1), HEALTH(2), HEALTH_ACK(3), FILE_UPLOAD(4), ELECTION(5), NEIGHBOUR_INFO(6), RECOVERY(7);

    public final byte command;

    Command(int command) {
        this.command = ((byte) command);
    }
}
