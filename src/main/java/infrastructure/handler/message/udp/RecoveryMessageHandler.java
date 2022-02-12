package infrastructure.handler.message.udp;

import infrastructure.Command;
import infrastructure.client.RemoteClient;
import infrastructure.converter.PayloadConverter;
import infrastructure.system.FileChunk;
import infrastructure.system.IdService;
import infrastructure.system.RemoteNode;
import infrastructure.system.SystemContext;
import infrastructure.system.message.RecoveryMessage;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;

public class RecoveryMessageHandler implements UdpMessageHandler {
    private final RemoteClient<DatagramPacket> client;
    private final PayloadConverter<RecoveryMessage> converter;

    public RecoveryMessageHandler(RemoteClient<DatagramPacket> client, PayloadConverter<RecoveryMessage> converter) {
        this.client = client;
        this.converter = converter;
    }

    @Override
    public void handle(SystemContext context, DatagramPacket message) {
        RecoveryMessage recoveryMessage = converter.decode(message.getData());

        if (context.isLeader()) {
            if (!context.id.equals(IdService.nodeId(recoveryMessage.node().ip(), recoveryMessage.node().port()))) {
                HashMap<String, List<FileChunk>> chunksDistributionTable = context.getLeaderContext().chunksDistributionTable;

                context.getLeaderContext().aliveNodes.put(recoveryMessage.node(), 0);
                recoveryMessage.fileChunks()
                        .forEach(chunkName -> {
                            String fileName = chunkName.substring(0, chunkName.lastIndexOf('-'));

                            if (chunksDistributionTable.containsKey(fileName)) {
                                chunksDistributionTable.get(fileName).add(new FileChunk(chunkName, recoveryMessage.node()));
                            } else {
                                ArrayList<FileChunk> fileChunks = new ArrayList<>();
                                fileChunks.add(new FileChunk(chunkName, recoveryMessage.node()));
                                chunksDistributionTable.put(fileName, fileChunks);
                            }
                        });
            }
        } else {
            try {
                RemoteNode current = new RemoteNode(InetAddress.getLocalHost(), context.listenPort);
                client.unicast(converter.encode(Command.RECOVERY, new RecoveryMessage(current, getStoredChunks())),
                        context.getLeader().ip(), context.getLeader().port());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getStoredChunks() {
        File files = new File("files/");
        if (files.exists() && files.listFiles() != null) {
            return Arrays.stream(files.listFiles()).map(File::getName).toList();
        }
        return Collections.emptyList();
    }
}
