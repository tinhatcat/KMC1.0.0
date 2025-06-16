import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
 

public class AppendCHash{


public AppendCHash() throws IOException {
        appendConsensusHash();
    }
    
    public void appendConsensusHash() throws IOException {
        String ledgerPath = "ledger_KMC/ledger_current.txt";
        String hashPath = "ledger_KMC/consensus_HASH.log";
        
        try (FileWriter writer = new FileWriter(ledgerPath, true)) {
            String hashContent = Files.readString(Paths.get(hashPath));
            writer.write("=" + hashContent + " ");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw e;
        }
    }
}