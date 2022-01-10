package infrastructure.converter;

import infrastructure.handler.message.Message;

public interface PayloadConverter<T> {
    Message<T> convert(byte[] payload);
}
