package scripts;

import configuration.Configuration;
import infrastructure.Command;
import infrastructure.Node;
import infrastructure.client.ReliableOrderedUdpClient;
import infrastructure.client.RemoteClient;
import infrastructure.client.UdpClient;
import infrastructure.converter.*;
import infrastructure.system.RemoteNode;
import infrastructure.system.message.FileDeletionMessage;
import infrastructure.system.message.FileEditMessage;
import infrastructure.system.message.FileReadMessage;
import infrastructure.system.message.FileUploadMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Demo {
    private final RemoteClient<DatagramPacket> client = new ReliableOrderedUdpClient(new ResendPayloadConverter(), new UdpClient(), new Configuration(new String[]{}).getContext());
    public static String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";

    public static void main(String[] args) throws IOException {
        Demo demo = new Demo();

        demo.fileUpload1();
        demo.fileUpload2();
        demo.fileEdit();
        demo.fileDelete();

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        demo.readFile("lorem_ipsum");
    }

    public void fileUpload1() throws IOException {
        fileUpload("abc", "abc");
    }

    public void fileUpload2() throws IOException {
        fileUpload("lorem_ipsum", loremIpsum);
    }

    private void fileUpload(String fileName, String content) throws IOException {
        System.out.println("Uploading file " + fileName + " with content " + content);
        FileUploadConverter converter = new FileUploadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileUploadMessage message = new FileUploadMessage(fileName, content.getBytes(StandardCharsets.UTF_8));
        client.unicast(converter.encode(Command.FILE_UPLOAD, message), Node.getLocalIp(), 4712);
        System.out.println("File uploaded");
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage(fileName, null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(), 4712);

        try (DatagramSocket socket = new DatagramSocket(4713)) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[10000], 10000);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        }
    }

    public void fileEdit() throws IOException {
        System.out.println("Editing file abc with content: adc");
        FileEditConverter editConverter = new FileEditConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        FileEditMessage editMessage = new FileEditMessage("abc", "adc".getBytes(StandardCharsets.UTF_8));
        client.unicast(editConverter.encode(Command.FILE_EDIT, editMessage), Node.getLocalIp(), 4712);
        System.out.println("File edited");

        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(), 4712);

        try(DatagramSocket socket = new DatagramSocket(4713)) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        }
    }

    public void fileDelete() throws IOException {
        System.out.println("Deleting file abc");
        FileDeletionPayloadConverter deleteConverter = new FileDeletionPayloadConverter();
        FileReadConverter fileReadConverter = new FileReadConverter();
        client.unicast(deleteConverter.encode(Command.FILE_DELETE, new FileDeletionMessage("abc")), Node.getLocalIp(), 4712);
        System.out.println("File deleted");
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage("abc", null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(),  4712);

        try(DatagramSocket socket = new DatagramSocket(4713)){
            socket.setSoTimeout(5000);
            DatagramPacket datagramPacket = new DatagramPacket(new byte[100], 100);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        } catch (SocketTimeoutException e) {
            System.out.println("File not found");
        }
    }

    private void readFile(String name) throws IOException {
        FileReadConverter fileReadConverter = new FileReadConverter();
        client.unicast(fileReadConverter.encode(Command.FILE_READ, new FileReadMessage(name, null, new RemoteNode(Node.getLocalIp(), 4713))),
                Node.getLocalIp(),  4712);

        try (DatagramSocket socket = new DatagramSocket(4713)) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[10000], 10000);
            socket.receive(datagramPacket);
            FileReadMessage readMessage = fileReadConverter.decode(Arrays.copyOfRange(datagramPacket.getData(), 4, datagramPacket.getData().length));
            System.out.println("Read file: " + readMessage.fileName() + " with content: " + new String(readMessage.file(), StandardCharsets.UTF_8));
        }
    }
}
