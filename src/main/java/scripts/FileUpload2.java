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

public class FileUpload2 {

    public static void main(String[] args) throws IOException {
            }
}
