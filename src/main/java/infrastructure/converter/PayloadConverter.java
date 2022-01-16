package infrastructure.converter;

public interface PayloadConverter<T> {
    T convert(byte[] payload);
}
