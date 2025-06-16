import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class UpdateBlockBalance {
    
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
    
    // File paths
    private static final Path PLAYER_INFO_PATH = Paths.get("ledger_KMC/player_info.log");
    private static final Path LAST_PLAYER_PATH = Paths.get("Program_Files/lastplayerledger.log");
    private static final Path LAST_BLOCK_PATH = Paths.get("Program_Files/lastblockledger.log");
    private static final Path LAST_HASH_PATH = Paths.get("Program_Files/lastblockhash.log");
    private static final Path FIRST_BLOCK_PATH = Paths.get("first_block_mined.log");
    
    /**
     * Constructor that initiates the block balance update process
     * @throws IOException if file operations fail
     */
    public UpdateBlockBalance() throws IOException {
        updateBalance();
    }
    
    /**
     * Main method to update player balance based on block mining
     * @throws IOException if file operations fail
     */
    private void updateBalance() throws IOException {
        PlayerData playerData = loadPlayerData();
        BlockData blockData = loadBlockData();
        
        boolean playerFound = updateExistingPlayer(playerData, blockData);
        
        if (!playerFound) {
            addNewPlayer(playerData, blockData);
        }
    }
    
    /**
     * Loads player information from files
     * @return PlayerData object containing player information
     * @throws IOException if file operations fail
     */
    private PlayerData loadPlayerData() throws IOException {
        String playerInfoContent = Files.readString(PLAYER_INFO_PATH);
        String playerWithPrefix = Files.readString(LAST_PLAYER_PATH);
        String player = playerWithPrefix.substring(1); // Remove first character
        String hash = Files.readString(LAST_HASH_PATH);
        
        return new PlayerData(playerInfoContent, player.trim(), hash);
    }
    
    /**
     * Loads block information from files
     * @return BlockData object containing block information
     * @throws IOException if file operations fail
     */
    private BlockData loadBlockData() throws IOException {
        String blockContent = Files.readString(LAST_BLOCK_PATH);
        int dotIndex = blockContent.indexOf('.');
        String blockNumber = blockContent.substring(0, dotIndex);
        int blockNum = Integer.parseInt(blockNumber);
        
        return new BlockData(blockContent, blockNumber, blockNum);
    }
    
    /**
     * Updates balance for existing player
     * @param playerData player information
     * @param blockData block information
     * @return true if player was found and updated, false otherwise
     * @throws IOException if file operations fail
     */
    private boolean updateExistingPlayer(PlayerData playerData, BlockData blockData) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_INFO_PATH.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(playerData.playerName)) {
                    String wallet = reader.readLine();
                    String balance = reader.readLine();
                    
                    BigInteger currentBalance = new BigInteger(balance);
                    BigInteger reward = calculateReward(blockData.blockNumber);
                    BigInteger newBalance = currentBalance.add(reward);
                    
                    updatePlayerFile(playerData, wallet, balance, newBalance);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Updates the player info file with new balance
     * @param playerData player information
     * @param wallet player's wallet
     * @param oldBalance old balance as string
     * @param newBalance new balance as BigInteger
     * @throws IOException if file operations fail
     */
    private void updatePlayerFile(PlayerData playerData, String wallet, String oldBalance, BigInteger newBalance) throws IOException {
        String oldEntry = playerData.playerName + "\n" + wallet + "\n" + oldBalance;
        String newEntry = playerData.playerName + "\n" + wallet + "\n" + newBalance;
        
        try {
            String updatedContent = playerData.fileContent.replaceAll(
                java.util.regex.Pattern.quote(oldEntry), 
                java.util.regex.Matcher.quoteReplacement(newEntry)
            );
            Files.writeString(PLAYER_INFO_PATH, updatedContent, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error updating player file: " + e.getMessage());
        }
    }
    
    /**
     * Adds a new player to the system
     * @param playerData player information
     * @param blockData block information
     * @throws IOException if file operations fail
     */
    private void addNewPlayer(PlayerData playerData, BlockData blockData) throws IOException {
        if (playerData.hash.trim().isEmpty()) {
            return; // Skip if no hash provided
        }
        
        BigInteger reward = calculateReward(blockData.blockNumber);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PLAYER_INFO_PATH.toFile(), true))) {
            writer.write(playerData.playerName + "\n");
            writer.write("@" + blockData.blockNumberStr + "\n");
            writer.write(reward + "\n");
            writer.write("0\n");
            writer.write("0\n");
            writer.write(playerData.hash.replaceAll(" ", "") + "\n");
        }
        
        writeFirstBlock(blockData.blockContent);
    }
    
    /**
     * Calculates reward based on block number
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
    
    /**
     * Writes the first block information to file
     * @param blockContent the block content to write
     * @throws IOException if file operations fail
     */
    private void writeFirstBlock(String blockContent) throws IOException {
        Files.writeString(FIRST_BLOCK_PATH, blockContent, StandardCharsets.UTF_8);
    }
    
    /**
     * Inner class to hold player data
     */
    private static class PlayerData {
        final String fileContent;
        final String playerName;
        final String hash;
        
        PlayerData(String fileContent, String playerName, String hash) {
            this.fileContent = fileContent;
            this.playerName = playerName;
            this.hash = hash;
        }
    }
    
    /**
     * Inner class to hold block data
     */
    private static class BlockData {
        final String blockContent;
        final String blockNumberStr;
        final int blockNumber;
        
        BlockData(String blockContent, String blockNumberStr, int blockNumber) {
            this.blockContent = blockContent;
            this.blockNumberStr = blockNumberStr;
            this.blockNumber = blockNumber;
        }
    }
}