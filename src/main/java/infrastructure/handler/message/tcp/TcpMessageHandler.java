package infrastructure.handler.message.tcp;

import infrastructure.handler.message.MessageHandler;
import infrastructure.handler.message.ReliableMessageHandler;
import infrastructure.system.SystemContext;

import java.net.Socket;

public interface TcpMessageHandler extends ReliableMessageHandler<byte[]> {
}
