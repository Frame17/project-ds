package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.StartMessage;

import java.nio.ByteBuffer;

public class StartPayloadConverter implements PayloadConverter<StartMessage> {

    @Override
    public StartMessage decode(byte[] payload) {
        ByteBuffer message = ByteBuffer.wrap(payload);
        StartMessage startMessage = new StartMessage(message.getInt(1));

        return startMessage;
    }

    @Override
    public byte[] encode(Command command, StartMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);

        buffer.put(command.command);
        buffer.putInt(message.port());

        return buffer.array();
    }
}
