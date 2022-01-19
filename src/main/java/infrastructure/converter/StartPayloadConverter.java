package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.StartMessage;

import java.nio.ByteBuffer;

public class StartPayloadConverter implements PayloadConverter<StartMessage> {
    @Override
    public StartMessage convert(byte[] payload) {

        ByteBuffer message = ByteBuffer.wrap(payload);
        StartMessage startMessage = new StartMessage(message.getInt(1));

        return startMessage;
    }

    @Override
    public StartMessage decode(byte[] payload) {
        return this.convert(payload);
    }

    @Override
    public byte[] encode(Command c, StartMessage record) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);

        buffer.put(c.command);
        buffer.putInt(record.port());

        return buffer.array();
    }
}
