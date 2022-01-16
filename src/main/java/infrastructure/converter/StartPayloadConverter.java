package infrastructure.converter;

import java.nio.ByteBuffer;

public class StartPayloadConverter implements PayloadConverter<Integer> {
    @Override
    public Integer convert(byte[] payload) {
        ByteBuffer message = ByteBuffer.wrap(payload);
        return message.getInt(1);
    }
}
