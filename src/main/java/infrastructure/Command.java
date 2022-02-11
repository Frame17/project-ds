package infrastructure;

public enum Command {
    START(0), START_ACK(1), HEALTH(2), HEALTH_ACK(3), FILE_UPLOAD(4), FILE_EDIT(5), ELECTION(6), NEIGHBOUR_INFO(7), RESEND(8);

    public final byte command;

    Command(int command) {
        this.command = ((byte) command);
    }
}
