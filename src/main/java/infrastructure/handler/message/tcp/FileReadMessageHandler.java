package infrastructure.handler.message.tcp;

import infrastructure.Command;
import infrastructure.client.ReliableRemoteClient;
import infrastructure.handler.message.udp.ElectionMessageHandler;
import infrastructure.system.FileChunk;
import infrastructure.system.SystemContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class FileReadMessageHandler implements TcpMessageHandler{

    private final static Logger LOG = LogManager.getLogger(FileReadMessageHandler.class);

    private final ReliableRemoteClient<byte[]> client;

    public FileReadMessageHandler(ReliableRemoteClient<byte[]> client) {
        this.client = client;
    }


    @Override
    public void handle(SystemContext context, byte[] message, Socket socket) {

        ByteBuffer buffer = ByteBuffer.wrap(message, 1, message.length - 1);
        int fileNameLength = buffer.getInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        LOG.info("Read file {}", fileName);

        if (context.isLeader()) {

            // Someone wants to read the whole File...
            HashMap <String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;
            List<FileChunk> fileChunks = chunksDistributionTable.get(fileName);
            if(fileChunks == null){
                throw new RuntimeException(String.format("FileNotFound %s", fileName));
            }
            try{
                // The output stream to the request client
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());

                for (FileChunk fileChunk : fileChunks){
                    byte[] chunkName = fileChunk.name().getBytes(StandardCharsets.UTF_8);

                    ByteBuffer messageBuffer = ByteBuffer.allocate(Byte.BYTES +  chunkName.length);
                    messageBuffer.put(Command.FILE_READ.command);
                    messageBuffer.put(chunkName);

                    try{
                        Socket clientSocket = client.unicast(
                                messageBuffer.array(),
                                fileChunk.node().ip(),
                                fileChunk.node().port());

                        // the requested chunk is the inputstream
                        //bufferedOutputStream.write(clientSocket.getInputStream().readAllBytes());
                        //clientSocket.getInputStream().transferTo(bufferedOutputStream);

                        InputStream in = new BufferedInputStream(clientSocket.getInputStream());

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[4];
                        while ((nRead = in.readNBytes(data, 0, data.length)) != 0) {
                            byteArrayOutputStream.write(data, 0, nRead);
                        }
                        byteArrayOutputStream.flush();

                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        clientSocket.getOutputStream().write(bytes);



                        clientSocket.close();

                    }catch (IOException e){
                        LOG.error(e);
                    }
                }
                bufferedOutputStream.flush();


            }catch (IOException e){
                LOG.error(e);
            }


        }else {
            //just reply with the file
            try {

                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(readFile(fileName));
                outputStream.flush();

                //TODO: Close ? or happens this through socket.close() ?
            } catch (IOException e) {
                LOG.error(e);
            }
        }

    }
    private byte[] readFile(String fileName) throws IOException {
        return Files.readAllBytes(new File(fileName).toPath());
    }
}
