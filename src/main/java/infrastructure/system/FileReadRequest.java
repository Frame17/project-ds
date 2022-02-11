package infrastructure.system;

import java.util.Map;

public record FileReadRequest(String fileName, Map<String, byte[]> chunks) {

    public boolean isComplete(){
        return chunks.entrySet().stream().noneMatch(chunk -> chunk.getValue().length == 0);
    }
}