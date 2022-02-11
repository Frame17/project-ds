package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.*;
import infrastructure.system.message.FileReadDataMessage;
import infrastructure.system.message.FileReadMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.min;

public class FileReadMessageHandler implements TcpMessageHandler {

    private final static Logger LOG = LogManager.getLogger(FileReadMessageHandler.class);

    private final RemoteClient<byte[]> client;

    private final PayloadConverter<FileReadMessage> fileReadPayloadConverter;
    private final PayloadConverter<FileReadDataMessage> fileReadDataPayloadConverter;

    public FileReadMessageHandler(RemoteClient<byte[]> client,PayloadConverter<FileReadMessage> fileReadPayloadConverter, PayloadConverter<FileReadDataMessage> fileReadDataPayloadConverter) {
        this.client = client;
        this.fileReadPayloadConverter = fileReadPayloadConverter;
        this.fileReadDataPayloadConverter = fileReadDataPayloadConverter;
    }

    @Override
    public void handle(SystemContext context, byte[] message, RemoteNode sender) {

        FileReadMessage fileReadMessage = fileReadPayloadConverter.decode(message);

        LOG.info("File-Read-Request {} from {}", fileReadMessage.fileName(), sender.ip().getHostAddress());

        if (context.isLeader()) {

            List<FileChunk> fileChunks = context.getLeaderContext().chunksDistributionTable.get(fileReadMessage.fileName());

            // Filter chunks, get each Chunk::name only once... can happen, if replication is enabled...
            // Sort the Chunks...
            List<FileChunk> distinctChunks = fileChunks.stream()
                    .filter(distinctByKey(FileChunk::name))
                    .sorted(
                            Comparator.comparing(o -> Integer.valueOf(
                                    o.name().substring(o.name().lastIndexOf('-') +1 ))))
                    .toList();

            FileRequest fileRequest = new FileRequest(fileReadMessage.fileName(), new ArrayList<>());

            // Ask the RemoteNodes for each chunk
            for (FileChunk chunk : distinctChunks) {
                try {
                    byte[] chunkData = null;

                    // If the chunk is stored locally, read the file direct
                    if(IdService.nodeId(chunk.node().ip(), chunk.node().port()).equals(context.id)){
                        chunkData = readFile(chunk.name());
                    }else{
                        FileReadMessage fileReadMessageSend = new FileReadMessage(chunk.name(), fileReadMessage.ip(), fileReadMessage.port());
                        client.unicast(fileReadPayloadConverter.encode(Command.FILE_READ, fileReadMessageSend), chunk.node().ip(), chunk.node().port());
                    }

                    fileRequest.chunks().add(new Pair<>(chunk, chunkData));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            RemoteNode requesterRemoteNode = new RemoteNode(fileReadMessage.ip(), fileReadMessage.port());
            if (!context.getLeaderContext().fileReadRequest.containsKey(requesterRemoteNode)) {
                context.getLeaderContext().fileReadRequest.put(requesterRemoteNode, new ArrayList<>());
            }
            // Add new FileRequest to the list of the RemoteNode
            context.getLeaderContext().fileReadRequest.get(requesterRemoteNode).add(fileRequest);


        } else {
            try {
                // Read File from Disk
                byte[] fileBytes = readFile(fileReadMessage.fileName());

                FileReadDataMessage fileReadDataMessage = new FileReadDataMessage(
                        fileReadMessage.fileName(),fileBytes,fileReadMessage.ip(), fileReadMessage.port());
                client.unicast(fileReadDataPayloadConverter.encode(Command.FILE_READ_DATA, fileReadDataMessage), sender.ip(), sender.port());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private byte[] readFile(String fileName) throws IOException {
        return Files.readAllBytes(new File(fileName).toPath());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }



}
