package infrastructure.converter;

import infrastructure.Command;
import infrastructure.system.message.ResendMessage;

public class ResendPayloadConverter implements PayloadConverter<ResendMessage> {

    //todo - implement
    @Override
    public ResendMessage decode(byte[] payload) {
        return null;
    }

    @Override
    public byte[] encode(Command command, ResendMessage message) {
        return null;
    }
}
