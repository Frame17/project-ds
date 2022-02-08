package infrastructure.system.message;

public record FileUploadMessage(String fileName, byte[] file) {
}
