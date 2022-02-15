package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.handler.message.udp.UdpMessageHandler;
import infrastructure.system.FileChunk;
import infrastructure.system.IdService;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileDeletionMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.List;

public class FileDeletionMessageHandler implements UdpMessageHandler {
    private final static Logger LOG = LogManager.getLogger(FileDeletionMessageHandler.class);
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<FileDeletionMessage> converter;

    public FileDeletionMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<FileDeletionMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket packet) {
        FileDeletionMessage deletionMessage = converter.decode(packet.getData());
        LOG.info("File-Delete-Request {}", deletionMessage.filename());
        //when the leader receives a deletion message he needs to find all the nodes which have chunks of the data. Then it resends the deletion message with the corresponding file fileName
        //to the found nodes
        if (context.isLeader()) {
            HashMap<String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;

            if (chunksDistributionTable.containsKey(deletionMessage.filename())) {
                List<FileChunk> fileChunkList = chunksDistributionTable.get(deletionMessage.filename());

                for (FileChunk fileChunk : fileChunkList) {
                    //for each client in the fileChunkList a message is sent
                    String chunkName = fileChunk.name();
                    RemoteNode target = fileChunk.node();
                    FileDeletionMessage chunkMessage = new FileDeletionMessage(chunkName);
                    if (context.id.equals(IdService.nodeId(target.ip(), target.port()))) {
                        deleteFile(chunkName, context);
                    } else {
                        try {
                            client.unicast(converter.encode(Command.FILE_DELETE, chunkMessage), target.ip(), target.port() + 1);
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    }
                }
                chunksDistributionTable.remove(deletionMessage.filename());
            } else {
                LOG.info(context.id + "File {} has not been found", deletionMessage.filename());
            }
        } else {
            deleteFile(deletionMessage.filename(), context);
        }
    }

    private void deleteFile(String fileName, SystemContext context) {
        File fileToDelete = new File("files/" + fileName);
        if (fileToDelete.delete()) {
            LOG.info(context.id + " file chunk {} has been deleted", fileName);
        } else {
            LOG.info(context.id + " file chunk {} could not be deleted", fileName);
        }
    }
}
