package scripts;

import configuration.Configuration;
import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.FileEditConverter;
import infrastructure.converter.FileReadConverter;
import infrastructure.converter.FileUploadConverter;
import infrastructure.converter.ResendPayloadConverter;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.FileEditMessage;
import infrastructure.system.message.FileReadMessage;
import infrastructure.system.message.FileUploadMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FileEdit {
    public static void main(String[] args) throws IOException {
        final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
        FileUploadConverter uploadConverter = new FileUploadConverter();
        FileEditConverter editConverter = new FileEditConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage uploadMessage = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        FileEditMessage editMessage = new FileEditMessage("abc", "adc".getBytes(StandardCharsets.UTF_8));
        client.unicast(uploadConverter.encode(Command.FILE_UPLOAD, uploadMessage), Node.getLocalIp(), 4712);
        client.unicast(editConverter.encode(Command.FILE_EDIT, editMessage), Node.getLocalIp(), 4712);

        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(InetAddress.getLocalHost(), 4713))),
                Node.getLocalIp(), 4712);

        DatagramSocket socket = new DatagramSocket(4713);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
        socket.receive(datagramPacket);
        FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
        System.out.println(readMessage.fileName() + " : " + new String(readMessage.file(), StandardCharsets.UTF_8));
    }
}
