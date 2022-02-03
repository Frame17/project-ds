package infrastructure.handler.message.tcp;

import infrastructure.system.SystemContext;

public class FileDeletionMessageHandler implements TcpMessageHandler{
    // ToDo implement handler
    // is a Leader node function. recives a message from a client with the file name?? in the message
    // searches for filename. gets all locations from file chunks and deletes the corresponding files
    @Override
    public void handle(SystemContext context, byte[] message) {

    }
}
