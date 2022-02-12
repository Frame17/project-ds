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

public class Demo {
    private final static RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
    public static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";

    public static void main(String[] args) throws IOException {
        fileUpload1();
        fileUpload2();
    }

    public static void fileUpload1() throws IOException {
        FileUploadConverter converter = new FileUploadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage message = new FileUploadMessage("abc", "abc".getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), Node.getLocalIp(), 4712);
        System.out.println("file sent");
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(), 4712);

        try (DatagramSocket socket = new DatagramSocket(4713)) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        }
    }

    public static void fileUpload2() throws IOException {
        FileUploadConverter converter = new FileUploadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage message = new FileUploadMessage("lorem_ipsum", loremIpsum.getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), Node.getLocalIp(), 4712);
        System.out.println("file sent");
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("lorem_ipsum", null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(), 4712);

        try (DatagramSocket socket = new DatagramSocket(4713)) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[10000], 10000);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        }
    }
}
