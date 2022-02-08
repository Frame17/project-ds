package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.SystemContext;
import infrastructure.system.message.FileEditMessage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Math.ceil;

public class FileEditMessageHandler implements TcpMessageHandler {
    private final RemoteClient<byte[]> client;
    private final PayloadConverter<FileEditMessage> converter;

    public FileEditMessageHandler(RemoteClient<byte[]> client, PayloadConverter<FileEditMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, byte[] message) {
        FileEditMessage fileEditMessage = converter.decode(message);

        if (context.isLeader()) {
            ByteBuffer buffer = ByteBuffer.wrap(fileEditMessage.file());
            int chunkSize = (int) ceil((double) fileEditMessage.file().length / (1 + context.getLeaderContext().aliveNodes.size()));
            byte[] chunk = new byte[chunkSize];
            buffer.get(chunk);
            replaceFile(fileEditMessage.fileName() + "-0", chunk);

            context.getLeaderContext().chunksDistributionTable.get(fileEditMessage.fileName())
                    .forEach(fileChunk -> {
                        byte[] editChunk = new byte[chunkSize];
                        buffer.get(editChunk);

                        try {
                            client.unicast(converter.encode(Command.FILE_EDIT, new FileEditMessage(fileChunk.name(), editChunk)),
                                    fileChunk.node().ip(), fileChunk.node().port());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
            replaceFile(fileEditMessage.fileName(), fileEditMessage.file());
        }
    }

    private void replaceFile(String fileName, byte[] content) {
        try {
            Path path = new File(fileName).toPath();
            Files.delete(path);
            Files.write(path, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
