package infrastructure;

public enum Command {
    START(0), START_ACK(1), HEALTH(2), HEALTH_ACK(3), FILE_UPLOAD(4), FILE_EDIT(5), FILE_DELETE(6), FILE_READ(7), ELECTION(8), NEIGHBOUR_INFO(9), RESEND(10);

    public final byte command;

    Command(int command) {
        this.command = ((byte) command);
    }
}
