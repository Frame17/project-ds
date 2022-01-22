package infrastructure.converter;

import java.nio.ByteBuffer;

public class HealthPayloadConverter implements PayloadConverter<Integer>{
    @Override
    public Integer convert(byte[] payload) {
        // Each Node sends it´s node ID which will be used in the HashMap to update the counter
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        return buffer.getInt();
    }
}
