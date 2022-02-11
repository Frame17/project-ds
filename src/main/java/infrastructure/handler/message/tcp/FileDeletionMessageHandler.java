package infrastructure.handler.message.tcp;
import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.FileChunk;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileDeletionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class FileDeletionMessageHandler implements TcpMessageHandler {

    // ToDo implement handler
    // is a Leader node function. receives a message from a client with the file name?? in the message
    // searches for filename. gets all locations from file chunks and deletes the corresponding files
    private final static Logger LOG = LogManager.getLogger(FileDeletionMessageHandler.class);
    private final RemoteClient<byte[]> client;
    private final PayloadConverter<FileDeletionMessage> converter;

    public FileDeletionMessageHandler(RemoteClient<byte[]> client, PayloadConverter<FileDeletionMessage> converter) {
        this.client = client;
        this.converter = converter;

    }

    @Override
    public void handle(SystemContext context, byte[] message) {
        //decoding the message
        FileDeletionMessage deletionMessage = converter.decode(message);
        //when the leader receives a deletion message he needs to find all the nodes which have chunks of the data. Then it resends the deletion message with the corresponding file name
        //to the found nodes
        if (context.isLeader()) {
            HashMap<String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;
            // find filename in Distribution table
            if (chunksDistributionTable.containsKey(deletionMessage.Filename())) {
                // gets all the nodes in which the file is stored
                List<FileChunk> fileChunkList = chunksDistributionTable.get(deletionMessage.Filename());
                for (FileChunk fileChunk: fileChunkList) {
                    //for each node in the fileChunkList a message is sent
                    String chunkName = fileChunk.name();
                    RemoteNode target = fileChunk.node();
                    FileDeletionMessage chunkMessage = new FileDeletionMessage(chunkName.length(), chunkName);
                    if(chunkName.equals(deletionMessage.Filename()+"-0")){
                        File fileToDelete = new File(deletionMessage.Filename());
                        if(fileToDelete.delete()){
                            //log file chunk got deleted
                            LOG.info(context.id + "File chunk {} has been deleted", deletionMessage.Filename());
                        }else{
                            //log file chunk could not be deleted
                            LOG.info(context.id + "File chunk {} could not be deleted", deletionMessage.Filename());
                        }
                    }else {
                        try {
                            client.unicast(converter.encode(Command.FILE_DELETE, chunkMessage), target.ip(), target.port());
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    }
                }
                //after every deletion message has been sent. Remove Filename from list of chunks
                chunksDistributionTable.remove(deletionMessage.Filename());

            }else{
                // Log that file is not found
                LOG.info(context.id + "File {} has not been found", deletionMessage.Filename());
            }
        }else{
            //if a non leader receives the deletion Massages he needs to delete the file with the file name
                File fileToDelete = new File(deletionMessage.Filename());
                if(fileToDelete.delete()){
                    //log file chunk got deleted
                    LOG.info(context.id + "File chunk {} has been deleted", deletionMessage.Filename());
                }else{
                    //log file chunk could not be deleted
                    LOG.info(context.id + "File chunk {} could not be deleted", deletionMessage.Filename());
                }
        }
    }
}
