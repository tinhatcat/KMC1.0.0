
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; 

public class BackupLedger{


public BackupLedger()  throws IOException {
        backupLedgerFiles();
    }
    
    public void backupLedgerFiles() throws IOException {
        Path ledgerSource = Paths.get("ledger_KMC/ledger_current.txt");
        Path ledgerBackup = Paths.get("ledger_KMC/ledger_currentCOPY.txt");
        Path playerInfoSource = Paths.get("ledger_KMC/player_info.log");
        Path playerInfoBackup = Paths.get("ledger_KMC/player_infoCOPY.log");
        
        try {
            Files.copy(ledgerSource, ledgerBackup, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(playerInfoSource, playerInfoBackup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error copying files: " + e.getMessage());
            throw e;
        }
    }    
}