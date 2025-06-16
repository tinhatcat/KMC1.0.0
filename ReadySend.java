import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ReadySend {
    
    public ReadySend() throws IOException {
        copyConsensusHash();
    }
    
    public void rS() throws IOException {
        copyConsensusHash();
    }
    
    private void copyConsensusHash() throws IOException {
        Path source = Paths.get("ledger_KMC/consensus_HASH.log");
        Path destination = Paths.get("Program_Files/readySend.txt");
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
}