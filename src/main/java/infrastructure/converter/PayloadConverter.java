package infrastructure.converter;

import infrastructure.Command;

public interface PayloadConverter<T> {


    T decode(byte[] payload);
    byte[] encode(Command c, T record);
}
