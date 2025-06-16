import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class ReFormatLedger {
    
    private static final Path LOCAL_PLAYER_PATH = Paths.get("localplayer.txt");
    private static final Path WALLET_ADDRESS_PATH = Paths.get("wallet_address.log");
    
    // Reward amounts for different block ranges
    private static final BigInteger[] REWARDS = {
        new BigInteger("100000000000000"),  // Blocks 0-2100000
        new BigInteger("50000000000000"),   // Blocks 2100001-4200000
        new BigInteger("25000000000000"),   // Blocks 4200001-6300000
        new BigInteger("12500000000000"),   // Blocks 6300001-8400000
        new BigInteger("6250000000000"),    // Blocks 8400001-10500000
        new BigInteger("3125000000000"),    // Blocks 10500001-12600000
        new BigInteger("1562500000000"),    // Blocks 12600001-14700000
        new BigInteger("781250000000"),     // Blocks 14700001-16800000
        new BigInteger("390625000000"),     // Blocks 16800001-18900000
        new BigInteger("195312500000")      // Blocks 18900001-21000000
    };
    
    // Block range boundaries
    private static final int[] BLOCK_BOUNDARIES = {
        2100001, 4200001, 6300001, 8400001, 10500001,
        12600001, 14700001, 16800001, 18900001, 21000001
    };
    
    /**
     * Constructor that initializes the ledger reformatting process
     * @throws IOException if file operations fail
     */
    public ReFormatLedger() throws IOException {
        File lastBlockFile = new File("Program_Files/lastblock.log");
        
        if (lastBlockFile.exists() && lastBlockFile.length() != 0) {
            formatLedger();
            processLedgerForRewards();
        }
    }
    
    /**
     * Formats the ledger by replacing spaces with newlines
     * @throws IOException if file operations fail
     */
    private void formatLedger() throws IOException {
        File inputFile = new File("Program_Files/ledger_final.log");
        File outputFile = new File("Program_Files/ledger_formatted.log");
        
        // Clear the output file
        new PrintWriter(outputFile).close();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.replace(" ", "\n"));
            }
        }
    }
    
    /**
     * Processes the formatted ledger to calculate rewards for the local player
     * @throws IOException if file operations fail
     */
    private void processLedgerForRewards() throws IOException {
        File inputFile = new File("Program_Files/ledger_formatted.log");
        File outputFile = new File("Program_Files/hash2.log");
        
        BigInteger finalBalance = BigInteger.ZERO;
        String localPlayerName = getLocalPlayerName();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".")) {
                    int blockNumber = extractBlockNumber(line);
                    if (blockNumber > 0) {
                        String playerName = reader.readLine();
                        
                        if (isLocalPlayer(playerName, localPlayerName)) {
                            updateWalletAddress(String.valueOf(blockNumber));
                            finalBalance = finalBalance.add(calculateReward(blockNumber));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reads the local player name from file
     * @return the local player name, or empty string if not found
     */
    private String getLocalPlayerName() {
        try {
            return Files.readString(LOCAL_PLAYER_PATH);
        } catch (IOException e) {
            System.err.println("Error reading local player file: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Extracts block number from a line containing a period
     * @param line the line to parse
     * @return the block number, or -1 if invalid
     */
    private int extractBlockNumber(String line) {
        try {
            int periodIndex = line.indexOf('.');
            if (periodIndex > 0) {
                String blockNumberStr = line.substring(0, periodIndex);
                return Integer.parseInt(blockNumberStr);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid block number format: " + line);
        }
        return -1;
    }
    
    /**
     * Checks if the given player name matches the local player
     * @param playerName the player name to check
     * @param localPlayerName the local player name
     * @return true if they match
     */
    private boolean isLocalPlayer(String playerName, String localPlayerName) {
        return playerName != null && (" " + playerName).equals(localPlayerName);
    }
    
    /**
     * Updates the wallet address file with the block number
     * @param blockNumber the block number to write
     */
    private void updateWalletAddress(String blockNumber) {
        try {
            File walletFile = new File("wallet_address.log");
            if (walletFile.exists() && walletFile.length() == 0) {
                Files.writeString(WALLET_ADDRESS_PATH, blockNumber, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Error updating wallet address: " + e.getMessage());
        }
    }
    
    /**
     * Calculates the reward amount based on the block number
     * @param blockNumber the block number
     * @return the reward amount
     */
    private BigInteger calculateReward(int blockNumber) {
        for (int i = 0; i < BLOCK_BOUNDARIES.length; i++) {
            if (blockNumber < BLOCK_BOUNDARIES[i]) {
                return REWARDS[i];
            }
        }
        return BigInteger.ZERO; // No reward for blocks beyond the last boundary
    }
}