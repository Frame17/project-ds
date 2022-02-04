package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.system.FileChunk;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class FileDeletionMessageHandler implements TcpMessageHandler {

    // ToDo implement handler
    // is a Leader node function. receives a message from a client with the file name?? in the message
    // searches for filename. gets all locations from file chunks and deletes the corresponding files

    private final RemoteClient<byte[]> client;

    public FileDeletionMessageHandler(RemoteClient<byte[]> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, byte[] message) {
        //decoding the message. massage
        ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
        //when the leader receives a deletion message he needs to find all the nodes which have chunks of the data. Then it resends the deletion message with the corresponding file name
        //to the found nodes
        if (context.isLeader()) {
            HashMap<String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;
            // find filename in Distribution table
            if (chunksDistributionTable.containsKey(fileName)) {
                // gets all the nodes in which the file is stored
                List<FileChunk> fileChunkList = chunksDistributionTable.get(fileName);
                for (FileChunk fileChunk: fileChunkList) {
                    //for each node in the fileChunkList a message is sent
                    String chunkName = fileChunk.name();
                    RemoteNode target = fileChunk.node();
                    byte[] chunkNameBytes = chunkName.getBytes(StandardCharsets.UTF_8);
                    //encoding the message
                    ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileName.length());
                    messageBuffer.put(Command.FILE_DELETE.command);
                    messageBuffer.putInt(fileNameLength);
                    messageBuffer.put(chunkNameBytes);
                    try {
                        client.unicast(messageBuffer.array(), target.ip(), target.port());
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                }
                //after every deletion message has been sent. Remove Filename from list of chunks

            }else{
                // Log that file is not found
            }
        }else{
            //if a non leader receives the deletion Massages he needs to delete the file with the file name
        }
    }
}
