
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; 

public class BackupTxs{


public BackupTxs() throws IOException {
        backupTransactions();
    }
    
    public void backupTransactions() throws IOException {
        Path transactionSource = Paths.get("Program_Files/latestTxs4.log");
        Path transactionBackup = Paths.get("Program_Files/latestTxs4COPY.log");
        
        try {
            Files.copy(transactionSource, transactionBackup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error copying transaction file: " + e.getMessage());
            throw e; // Re-throw to maintain error handling contract
        }
    }
}