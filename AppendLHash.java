import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
 

public class AppendLHash{


public AppendLHash() throws IOException {
        appendLedgerHash();
    }
    
    public void appendLedgerHash() throws IOException {
        String hashLogPath = "ledger_KMC/ledgerhashes.log";
        String currentHashPath = "ledger_KMC/ledger_current_HASH.log";
        
        try (FileWriter writer = new FileWriter(hashLogPath, true)) {
            String hashContent = Files.readString(Paths.get(currentHashPath));
            writer.write(hashContent + " ");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw e;
        }
    }
}
