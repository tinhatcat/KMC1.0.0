import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MakeLedger - Combines block ledger, player ledger, and block hash data
 * into the current ledger file for blockchain record keeping.
 */
public class MakeLedger {
    
    private static final Logger LOGGER = Logger.getLogger(MakeLedger.class.getName());
    
    // File paths as constants
    private static final String LAST_BLOCK_LEDGER_PATH = "Program_Files/lastblockledger.log";
    private static final String LAST_PLAYER_LEDGER_PATH = "Program_Files/lastplayerledger.log";
    private static final String LAST_BLOCK_HASH_PATH = "Program_Files/lastblockhash.log";
    private static final String LEDGER_CURRENT_PATH = "ledger_KMC/ledger_current.txt";
    
    private final Path lastBlockLedgerPath;
    private final Path lastPlayerLedgerPath;
    private final Path lastBlockHashPath;
    private final Path ledgerCurrentPath;
    
    /**
     * Constructor - initializes paths and creates the ledger entry
     */
    public MakeLedger() throws IOException {
        this.lastBlockLedgerPath = Paths.get(LAST_BLOCK_LEDGER_PATH);
        this.lastPlayerLedgerPath = Paths.get(LAST_PLAYER_LEDGER_PATH);
        this.lastBlockHashPath = Paths.get(LAST_BLOCK_HASH_PATH);
        this.ledgerCurrentPath = Paths.get(LEDGER_CURRENT_PATH);
        
        createLedgerEntry();
    }
    
    /**
     * Creates a new ledger entry by combining block ledger, player ledger, and block hash
     * 
     * The format is: [block_ledger] [player_ledger_without_first_char] [block_hash_no_spaces]
     */
    private void createLedgerEntry() throws IOException {
        try {
            // Read the source files
            String blockLedgerContent = readFileContent(lastBlockLedgerPath, "block ledger");
            String playerLedgerContent = readFileContent(lastPlayerLedgerPath, "player ledger");
            String blockHashContent = readFileContent(lastBlockHashPath, "block hash");
            
            // Process the data
            String processedPlayerLedger = processPlayerLedgerContent(playerLedgerContent);
            String processedBlockHash = processBlockHashContent(blockHashContent);
            
            // Create the ledger entry
            String ledgerEntry = String.format("%s %s %s ", 
                blockLedgerContent, 
                processedPlayerLedger, 
                processedBlockHash);
            
            // Append to the current ledger
            appendToLedger(ledgerEntry);
            
            //LOGGER.info("Successfully created ledger entry");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ledger entry", e);
            throw e;
        }
    }
    
    /**
     * Reads content from a file with error handling
     */
    private String readFileContent(Path filePath, String fileDescription) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException(String.format("%s file does not exist: %s", 
                fileDescription, filePath));
        }
        
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new IOException(String.format("Failed to read %s file: %s", 
                fileDescription, filePath), e);
        }
    }
    
    /**
     * Processes player ledger content by removing the first character
     * This appears to remove a formatting character or delimiter
     */
    private String processPlayerLedgerContent(String playerLedgerContent) {
        if (playerLedgerContent == null || playerLedgerContent.isEmpty()) {
            LOGGER.warning("Player ledger content is empty");
            return "";
        }
        
        // Remove first character (likely a formatting character)
        return playerLedgerContent.length() > 1 ? 
            playerLedgerContent.substring(1) : "";
    }
    
    /**
     * Processes block hash content by removing all spaces
     */
    private String processBlockHashContent(String blockHashContent) {
        if (blockHashContent == null) {
            LOGGER.warning("Block hash content is null");
            return "";
        }
        
        return blockHashContent.replaceAll("\\s+", "");
    }
    
    /**
     * Appends the ledger entry to the current ledger file
     */
    private void appendToLedger(String ledgerEntry) throws IOException {
        try {
            Files.writeString(ledgerCurrentPath, ledgerEntry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IOException("Failed to append entry to ledger file", e);
        }
    }
    
    /**
     * Static factory method for creating a MakeLedger instance
     */
   // public static void createLedger() throws IOException {
        //new MakeLedger();
    //}
}