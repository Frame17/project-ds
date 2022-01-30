package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.HealthMessage;

import java.nio.ByteBuffer;

public class HealthPayloadConverter implements PayloadConverter<HealthMessage> {

    @Override
    public HealthMessage decode(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        return new HealthMessage(buffer.getInt());
    }

    @Override
    public byte[] encode(Command command, HealthMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
        buffer.put(Command.HEALTH.command);
        buffer.putInt(message.port());

        return buffer.array();
    }
}
