import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a distributed ledger system by copying ledger data to the first available
 * directory that has space. Manages multiple KMC (Kitty Mine Coin) directories
 * and handles file size limits for distributed storage.
 */
public class LedgerBuilder {
    
    // Mining rewards for different levels (halving pattern)
    private static final BigInteger[] MINING_REWARDS = {
        new BigInteger("100000000000000"),  // Level 1
        new BigInteger("50000000000000"),   // Level 2
        new BigInteger("25000000000000"),   // Level 3
        new BigInteger("12500000000000"),   // Level 4
        new BigInteger("6250000000000"),    // Level 5
        new BigInteger("3125000000000"),    // Level 6
        new BigInteger("1562500000000"),    // Level 7
        new BigInteger("781250000000"),     // Level 8
        new BigInteger("390625000000"),     // Level 9
        new BigInteger("195312500000")      // Level 10
    };
    
    private static final String LEDGER_BASE_DIR = "ledger_KMC";
    private static final String CURRENT_LEDGER_FILE = LEDGER_BASE_DIR + "/ledger_current.txt";
    private static final long MAX_FILE_SIZE_BYTES = 100_000_000; // 100MB
    private static final int MAX_FILES_PER_DIRECTORY = 100;
    private static final int TOTAL_KMC_DIRECTORIES = 12;
    private static final String REPLACEMENT_CHAR = "\\ufffd";
    
    private BigInteger miningBalance = BigInteger.ZERO;
    private BigInteger newThisProcessingBlock = BigInteger.ZERO;
    private final List<Path> kmcDirectories;
    
    public LedgerBuilder() throws IOException, NoSuchAlgorithmException {
        this.kmcDirectories = initializeDirectories();
        buildLedger();
    }
    
    /**
     * Initializes the list of KMC directories.
     * 
     * @return list of directory paths
     */
    private List<Path> initializeDirectories() {
        List<Path> directories = new ArrayList<>();
        for (int i = 1; i <= TOTAL_KMC_DIRECTORIES; i++) {
            directories.add(Paths.get(LEDGER_BASE_DIR, "KMC" + i));
        }
        return directories;
    }
    
    /**
     * Main method to build the ledger by copying current ledger data
     * to the first available directory with space.
     * 
     * @throws IOException if file operations fail
     * @throws NoSuchAlgorithmException if cryptographic operations fail
     */
    private void buildLedger() throws IOException, NoSuchAlgorithmException {
        Path currentLedgerPath = Paths.get(CURRENT_LEDGER_FILE);
        
        if (!Files.exists(currentLedgerPath)) {
            throw new FileNotFoundException("Current ledger file not found: " + CURRENT_LEDGER_FILE);
        }
        
        // Try each KMC directory in order until we find one with available space
        for (Path directory : kmcDirectories) {
            if (processDirectory(directory, currentLedgerPath)) {
                System.out.println("Ledger successfully written to directory: " + directory);
                return;
            }
        }
        
        throw new IOException("No available space found in any KMC directories");
    }
    
    /**
     * Processes a single KMC directory to find an available file for writing.
     * 
     * @param directory the directory to process
     * @param sourceLedgerPath path to the source ledger file
     * @return true if ledger was successfully written, false if no space available
     * @throws IOException if file operations fail
     */
    private boolean processDirectory(Path directory, Path sourceLedgerPath) throws IOException {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        
        File[] files = directory.toFile().listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        
        // Check each file in the directory for available space
        for (int i = 0; i < Math.min(files.length, MAX_FILES_PER_DIRECTORY); i++) {
            File file = files[i];
            
            if (file.length() < MAX_FILE_SIZE_BYTES) {
                // Found a file with available space
                copyLedgerToFile(sourceLedgerPath, file.toPath());
                return true;
            }
        }
        
        return false; // No files with available space found
    }
    
    /**
     * Copies the current ledger data to the specified target file.
     * Removes replacement characters during the copy process.
     * 
     * @param sourcePath path to the source ledger file
     * @param targetPath path to the target file
     * @throws IOException if file operations fail
     */
    private void copyLedgerToFile(Path sourcePath, Path targetPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(sourcePath);
             BufferedWriter writer = Files.newBufferedWriter(targetPath)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = line.replaceAll(REPLACEMENT_CHAR, "");
                writer.write(cleanedLine);
                writer.newLine();
            }
        }
    }
    
    /**
     * Gets the mining reward for a specific level.
     * 
     * @param level the mining level (1-10)
     * @return the mining reward amount
     * @throws IllegalArgumentException if level is out of range
     */
    public BigInteger getMiningReward(int level) {
        if (level < 1 || level > MINING_REWARDS.length) {
            throw new IllegalArgumentException("Invalid mining level: " + level);
        }
        return MINING_REWARDS[level - 1];
    }
    
    /**
     * Gets the current mining balance.
     * 
     * @return the current mining balance
     */
    public BigInteger getMiningBalance() {
        return miningBalance;
    }
    
    /**
     * Sets the mining balance.
     * 
     * @param balance the new mining balance
     */
    public void setMiningBalance(BigInteger balance) {
        this.miningBalance = balance;
    }
    
    /**
     * Gets the new processing block amount.
     * 
     * @return the new processing block amount
     */
    public BigInteger getNewThisProcessingBlock() {
        return newThisProcessingBlock;
    }
    
    /**
     * Sets the new processing block amount.
     * 
     * @param amount the new processing block amount
     */
    public void setNewThisProcessingBlock(BigInteger amount) {
        this.newThisProcessingBlock = amount;
    }
    
    /**
     * Creates the necessary directory structure for the ledger system.
     * 
     * @throws IOException if directory creation fails
     */
    public void initializeLedgerStructure() throws IOException {
        Files.createDirectories(Paths.get(LEDGER_BASE_DIR));
        
        for (Path directory : kmcDirectories) {
            Files.createDirectories(directory);
        }
    }
    
}