package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.*;
import infrastructure.system.message.FileReadDataMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class FileReadDataMessageHandler implements TcpMessageHandler {

    private final static Logger LOG = LogManager.getLogger(FileReadDataMessageHandler.class);

    private final RemoteClient<byte[]> client;
    private final PayloadConverter<FileReadDataMessage> fileReadDataPayloadConverter;

    public FileReadDataMessageHandler(RemoteClient<byte[]> client, PayloadConverter<FileReadDataMessage> fileReadDataPayloadConverter) {
        this.client = client;
        this.fileReadDataPayloadConverter = fileReadDataPayloadConverter;
    }

    @Override
    public void handle(SystemContext context, byte[] message, RemoteNode sender) {
        FileReadDataMessage dataMessage = fileReadDataPayloadConverter.decode(message);

        LOG.info("File-Read-Data {} from {}", dataMessage.fileName(), sender.ip().getHostAddress());

        String fileName = dataMessage.fileName();

        if (context.isLeader()) {
            // Receive Data from a Data-Node
            String originalFileName = fileName.substring(0, fileName.lastIndexOf('-'));

            RemoteNode initialRequester = new RemoteNode(dataMessage.ip(), dataMessage.port());

            // Find the FileRequest for this fileName from the "requester"
            FileRequest fileRequest =  context.getLeaderContext().fileReadRequest
                    .get(initialRequester)
                    .stream()
                    .filter(fileR -> fileR.name().equals(originalFileName))
                    .findFirst()
                    .orElse(null);

            if (fileRequest != null) {
                // Get Pair
                Pair<FileChunk, byte[]> currFileChunk = fileRequest.chunks().stream().filter(fileChunkPair -> fileChunkPair.getFirst().name().equals(fileName)).findFirst().orElse(null);

                if (currFileChunk != null) {
                    // Maybe write chunks direct to disc...
                    currFileChunk.setSecond(dataMessage.data());

                    // Send whole file back to Requester...
                    if (fileRequest.isComplete()){
                        int fileSize = fileRequest.chunks().stream().mapToInt(value -> value.getSecond().length).sum();

                        ByteBuffer fileBuffer = ByteBuffer.allocate(fileSize);

                        fileRequest.chunks().forEach(fileChunkPair -> {
                            fileBuffer.put(fileChunkPair.getSecond());
                        });

                        FileReadDataMessage readDataMessage = new FileReadDataMessage(fileRequest.name(), fileBuffer.array(), dataMessage.ip(), dataMessage.port());

                        try {
                            client.unicast(fileReadDataPayloadConverter.encode(Command.FILE_READ_DATA, readDataMessage), dataMessage.ip(), dataMessage.port());
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                    }
                }else{
                    LOG.info("Could not find matching File {} from {}", fileName, dataMessage.ip().getHostAddress());
                }
            }else{
                LOG.info("Could not find matching FileRequest for File {} from {}", originalFileName, dataMessage.ip().getHostAddress());
            }
        } else {
            // Im the Requester... Save the file
            //TODO
            saveFile(dataMessage.fileName(), dataMessage.data());
        }
    }

    private void saveFile(String fileName, byte[] content) {
        try {
            Files.write(new File(fileName).toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
