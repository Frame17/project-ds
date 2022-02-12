package scripts;

import configuration.Configuration;
import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.FileReadConverter;
import infrastructure.converter.FileUploadConverter;
import infrastructure.converter.ResendPayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.FileReadMessage;
import infrastructure.system.message.FileUploadMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FileUpload {
    public static void main(String[] args) throws IOException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
        FileUploadConverter converter = new FileUploadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage message = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), Node.getLocalIp(), 4712);
        System.out.println("file sent");
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(),  4712);

        DatagramSocket socket = new DatagramSocket(4713);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
        socket.receive(datagramPacket);
        FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
        System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
    }
}
