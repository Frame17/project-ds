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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.min;

public class FileReadMessageHandler implements TcpMessageHandler {

    private final static Logger LOG = LogManager.getLogger(FileReadMessageHandler.class);

    private final RemoteClient<byte[]> client;

    public FileReadMessageHandler(RemoteClient<byte[]> client) {
        this.client = client;
    }

    @Override
    public void handle(SystemContext context, byte[] message, RemoteNode sender) {
        ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);

        // IP of the initial requester
        byte[] ip = new byte[4 * Byte.BYTES];
        buffer.get(ip);
        InetAddress requesterAddress = null;

        try {
            requesterAddress = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        LOG.info("File-Read-Request {} from {}", fileName, sender.ip().getHostAddress());

        if (context.isLeader()) {

            List<FileChunk> fileChunks = context.getLeaderContext().chunksDistributionTable.get(fileName);

            // Filter chunks, get each Chunk::name only once... can happen, if replication is enabled...
            // Sort the Chunks...
            List<FileChunk> distinctChunks = fileChunks.stream()
                    .filter(distinctByKey(FileChunk::name))
                    .sorted(
                            Comparator.comparing(o -> Integer.valueOf(
                                    o.name().substring(o.name().lastIndexOf('-') +1 ))))
                    .toList();

            FileRequest fileRequest = new FileRequest(fileName, new ArrayList<>());

            // Ask for every chunk...
            for (FileChunk chunk : distinctChunks) {

                byte[] chunkName = chunk.name().getBytes(StandardCharsets.UTF_8);

                ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + chunkName.length + ip.length);
                messageBuffer.put(Command.FILE_READ.command);
                messageBuffer.putInt(chunkName.length);
                messageBuffer.put(chunkName);
                messageBuffer.put(ip);

                try {
                    client.unicast(messageBuffer.array(), chunk.node().ip(), chunk.node().port());
                    fileRequest.chunks().add(new Pair<>(chunk, null));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


            if (!context.getLeaderContext().fileReadRequest.containsKey(requesterAddress)) {
                context.getLeaderContext().fileReadRequest.put(requesterAddress, new ArrayList<>());
            }
            context.getLeaderContext().fileReadRequest.get(requesterAddress).add(fileRequest);


        } else {
            try {
                // Read File from Disk

                byte[] fileBytes = readFile(fileName);

                ByteBuffer fileBuffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + fileNameBytes.length + ip.length + fileBytes.length );
                fileBuffer.put(Command.FILE_READ_DATA.command);
                fileBuffer.putInt(fileNameBytes.length);
                fileBuffer.put(fileNameBytes);
                fileBuffer.put(ip);
                fileBuffer.put(fileBytes);

                client.unicast(fileBuffer.array(), sender.ip(), sender.port());


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
