package infrastructure.converter;

import infrastructure.system.RemoteNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class HealthPayloadConverter implements PayloadConverter<RemoteNode>{
    RemoteNode NodeID;

    @Override
    public RemoteNode convert(byte[] payload) {
        // Each Node sends it´s RemoteNode-Object which will be used in the HashMap to update the counter. This method will return the RemoteNode-Object
        ByteBuffer buffer = ByteBuffer.wrap(payload, 1, payload.length - 1);
        byte[] RemoteNodeArray = new byte[buffer.remaining()];
        buffer.get(RemoteNodeArray);
        ByteArrayInputStream bais = new ByteArrayInputStream(RemoteNodeArray);
        try{
            ObjectInput in = new ObjectInputStream(bais);
            NodeID = (RemoteNode) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return NodeID;
    }
}
