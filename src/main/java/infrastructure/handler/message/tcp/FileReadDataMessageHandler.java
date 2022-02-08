package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.system.*;
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

    public FileReadDataMessageHandler(RemoteClient<byte[]> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, byte[] message, RemoteNode sender) {
        ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
        int fileNameLength = buffer.getInt();

        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        // IP of the initial requester
        byte[] ip = new byte[4 * Byte.BYTES];
        buffer.get(ip);
        InetAddress requesterAddress = null;

        try {
            requesterAddress = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);


        LOG.info("File-Read-Data {} from {}", fileName, sender.ip().getHostAddress());

        if (context.isLeader()) {
            // Receive Data from a Data-Node
            String originalFileName = fileName.substring(0, fileName.lastIndexOf('-'));
            FileRequest fileRequest =  context.getLeaderContext().fileReadRequest
                    .get(requesterAddress)
                    .stream()
                    .filter(fileR -> fileR.name().equals(originalFileName))
                    .findFirst()
                    .orElse(null);

            if (fileRequest != null) {
                // Get Pair
                Pair<FileChunk, byte[]> currFileChunk = fileRequest.chunks().stream().filter(fileChunkPair -> fileChunkPair.getFirst().name().equals(fileName)).findFirst().orElse(null);

                if (currFileChunk != null) {
                    // Maybe write chunks direct to disc...
                    currFileChunk.setSecond(data);

                    // Send whole file back to Requester...
                    if (fileRequest.isComplete()){
                        int fileSize = fileRequest.chunks().stream().mapToInt(value -> value.getSecond().length).sum();


                        ByteBuffer fileBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileNameBytes.length + ip.length + fileSize);
                        fileBuffer.put(Command.FILE_READ_DATA.command);
                        fileBuffer.putInt(fileNameBytes.length);
                        fileBuffer.put(fileNameBytes);
                        fileBuffer.put(ip);

                        fileRequest.chunks().stream().forEach(fileChunkPair -> {
                            fileBuffer.put(fileChunkPair.getSecond());
                        });

                        try {
                            client.unicast(fileBuffer.array(), requesterAddress, 4711); //TODO port...
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


                }else{
                    LOG.info("Could not find matching File {} from {}", fileName, requesterAddress.getHostAddress());
                }

            }else{
                LOG.info("Could not find matching FileRequest for File {} from {}", originalFileName, requesterAddress.getHostAddress());
            }

        } else {
            // I'm the Requester ?

        }
    }

    private void writeFile(List<Pair<FileChunk,byte[]>> chunks) {

    }


}
