package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.RecoveryMessage;

import java.nio.ByteBuffer;

public class RecoveryPayloadConverter implements PayloadConverter<RecoveryMessage> {

    @Override
    public RecoveryMessage decode(byte[] payload) {
        return null;
    }

    @Override
    public byte[] encode(Command command, RecoveryMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + 4 * Byte.BYTES + Integer.BYTES);
        buffer.put(command.command);
        buffer.put(message.node().ip().getAddress());
        buffer.putInt(message.node().port());
        // todo - finish

        return buffer.array();
    }
}
