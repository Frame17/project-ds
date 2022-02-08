package infrastructure.system;

import java.util.ArrayList;
import java.util.List;

public record FileRequest (String name, List<Pair<FileChunk, byte[]>> chunks){


    public boolean isComplete(){
        return chunks().stream().noneMatch(fileChunkPair -> fileChunkPair.getSecond()== null);
    }
}