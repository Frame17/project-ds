package infrastructure.converter;

import infrastructure.Command;

public interface PayloadConverter<T> {

    @Deprecated
    T convert(byte[] payload);


    T decode(byte[] payload);
    byte[] encode(Command c, T record);
}
