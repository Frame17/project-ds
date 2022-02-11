package infrastructure.system.message;

public record FileEditMessage(String fileName, byte[] file) {
}
