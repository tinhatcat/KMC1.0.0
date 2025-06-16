import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class BlockchainSyncChecker {
    
    // File paths as constants
    private static final String OUTPUT_FILE = "output.txt";
    private static final String SYNCED_FILE = "Program_Files/synced.txt";
    private static final String DISCORD_FILE = "discordM.txt";
    private static final String LAST_BLOCK_LEDGER = "Program_Files/lastblockledger.log";
    private static final String LAST_PLAYER_LEDGER = "Program_Files/lastplayerledger.log";
    private static final String LAST_BLOCK_HASH = "Program_Files/lastblockhash.log";
    private static final String LAST_BLOCK = "Program_Files/lastblock.log";
    private static final String RESYNC_LOG = "Program_Files/resync.log";
    private static final String LEDGER_CURRENT = "ledger_KMC/ledger_current.txt";
    private static final String LEDGER_CURRENT_COPY = "ledger_KMC/ledger_currentCOPY.txt";
    private static final String PLAYER_INFO = "ledger_KMC/player_info.log";
    private static final String PLAYER_INFO_COPY = "ledger_KMC/player_infoCOPY.log";
    private static final String LATEST_TXS = "Program_Files/latestTxs.log";
    private static final String DISCORD_C = "discordC.txt";
    private static final String CONSENSUS_HASH = "ledger_KMC/consensus_HASH.log";
    
    private static final int CONSENSUS_THRESHOLD = 2;
    
    public BlockchainSyncChecker() throws IOException {
        performSyncCheck();
    }
    
    private void performSyncCheck() throws IOException {
        int syncingCount = 0;
        int unsyncCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(OUTPUT_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(SYNCED_FILE))) {
            
            String discordContent = readFileContent(DISCORD_FILE);
            String lastBlockContent = readFileContent(LAST_BLOCK_LEDGER);
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(discordContent)) {
                    System.out.println("Have a match");
                    syncingCount++;
                } else if (line.contains(".") && line.contains("=")) {
                    unsyncCount++;
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        
        handleSyncResults(syncingCount, unsyncCount);
    }
    
    private void handleSyncResults(int syncingCount, int unsyncCount) throws IOException {
        if (syncingCount >= CONSENSUS_THRESHOLD) {
            System.out.println("Player has matched at least 2/3 consensus!");
            clearFiles(OUTPUT_FILE, DISCORD_FILE);
        }
        
        if (unsyncCount >= CONSENSUS_THRESHOLD) {
            System.out.println("Player has been overtaken by consensus!");
            processUnsyncedBlocks();
        }
    }
    
    private void processUnsyncedBlocks() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(SYNCED_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                BlockData blockData = parseBlockData(line);
                
                String nextLine = reader.readLine();
                if (nextLine != null && nextLine.equals(line)) {
                    System.out.println("Other players have matched at least 2/3 consensus!");
                    handleConsensusMatch(blockData, line);
                }
            }
        }
        
        clearFiles(OUTPUT_FILE, DISCORD_FILE);
    }
    
    private BlockData parseBlockData(String line) {
        String[] parts = line.split(" ", 4);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid block data format: " + line);
        }
        
        String block = parts[0];
        String player = parts[1];
        String hash = parts[2];
        String remaining = parts[3];
        
        return new BlockData(block, player, hash, remaining);
    }
    
    private void handleConsensusMatch(BlockData blockData, String line) throws IOException {
        String ledgerContent = readFileContent(LAST_BLOCK_LEDGER);
        
        if (line.contains(ledgerContent)) {
            System.out.println("line3= " + line);
            System.out.println("Player received last block but it was written incorrectly...");
            handleMalformedBlock(blockData);
        } else {
            handleCorrectBlock(blockData);
        }
        
        if (containsTransactionMarkers(line)) {
            processTransactions(blockData, line);
        }
    }
    
    private void handleMalformedBlock(BlockData blockData) throws IOException {
        System.out.println("Restoring from backup files...");
        
        // Restore from backup files
        copyFile(LEDGER_CURRENT_COPY, LEDGER_CURRENT);
        copyFile(PLAYER_INFO_COPY, PLAYER_INFO);
        
        updateBlockLedgerFiles(blockData);
        writeToFile(RESYNC_LOG, "x", true);
    }
    
    private void handleCorrectBlock(BlockData blockData) throws IOException {
        updateBlockLedgerFiles(blockData);
        writeToFile(RESYNC_LOG, "x", true);
    }
    
    private void updateBlockLedgerFiles(BlockData blockData) throws IOException {
        writeToFile(LAST_PLAYER_LEDGER, " " + blockData.player, false);
        writeToFile(LAST_BLOCK_LEDGER, blockData.block, false);
        writeToFile(LAST_BLOCK_HASH, " " + blockData.hash, false);
        writeToFile(LAST_BLOCK, blockData.block, false);
    }
    
    private void processTransactions(BlockData blockData, String line) throws IOException {
        try {
            String txData = extractTransactionData(blockData.remaining);
            String walletAddress = extractWalletAddress(txData);
            String playerName = findPlayerByWallet(walletAddress);
            
            if (playerName != null) {
                String formattedTx = String.format("[00:00:00] [Render thread/INFO]: [CHAT] <%s>%s", 
                                                   playerName, txData);
                writeToFile(LATEST_TXS, formattedTx, true);
            }
        } catch (Exception e) {
            System.err.println("Error processing transactions: " + e.getMessage());
        }
    }
    
    private String extractTransactionData(String remaining) {
        int spaceIndex = remaining.indexOf(" ");
        int semiIndex = remaining.indexOf(";");
        
        if (spaceIndex != -1 && semiIndex != -1 && semiIndex > spaceIndex) {
            return remaining.substring(spaceIndex, semiIndex + 1);
        }
        return "";
    }
    
    private String extractWalletAddress(String txData) {
        int spaceIndex = txData.indexOf(" ");
        return spaceIndex != -1 ? txData.substring(0, spaceIndex) : txData;
    }
    
    private String findPlayerByWallet(String walletAddress) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_INFO))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String playerName = line;
                String nextLine = reader.readLine();
                
                if (nextLine != null && nextLine.equals("@" + walletAddress)) {
                    return playerName;
                }
            }
        }
        return null;
    }
    
    private boolean containsTransactionMarkers(String line) {
        return line.contains("&") && line.contains("_") && 
               line.contains(",") && line.contains("$");
    }
    
    public void rebuildTransactions() throws IOException {
        try (FileWriter writer = new FileWriter(SYNCED_FILE, true)) {
            String blockContent = readFileContent(LAST_BLOCK_LEDGER);
            String playerContent = readFileContent(LAST_PLAYER_LEDGER);
            String hashContent = readFileContent(LAST_BLOCK_HASH);
            String txContent = readFileContent(DISCORD_C);
            String consensusHash = readFileContent(CONSENSUS_HASH);
            
            String rebuiltData = String.format("%s%s%s%s =%s ", 
                                               blockContent, playerContent, hashContent, 
                                               txContent, consensusHash);
            writer.write(rebuiltData);
        }
    }
    
    // Utility methods
    private String readFileContent(String filePath) throws IOException {
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
            return "";
        }
    }
    
    private void writeToFile(String filePath, String content, boolean append) throws IOException {
        if (!append) {
            // Clear file first
            new PrintWriter(filePath).close();
        }
        
        try (FileWriter writer = new FileWriter(filePath, append)) {
            writer.write(content);
        }
    }
    
    private void copyFile(String sourcePath, String targetPath) throws IOException {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(targetPath), 
                      StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully: " + sourcePath + " -> " + targetPath);
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
            throw e;
        }
    }
    
    private void clearFiles(String... filePaths) throws IOException {
        for (String filePath : filePaths) {
            try {
                new PrintWriter(filePath).close();
            } catch (IOException e) {
                System.err.println("Error clearing file " + filePath + ": " + e.getMessage());
            }
        }
    }
    
    // Inner class to hold block data
    private static class BlockData {
        final String block;
        final String player;
        final String hash;
        final String remaining;
        
        BlockData(String block, String player, String hash, String remaining) {
            this.block = block;
            this.player = player;
            this.hash = hash;
            this.remaining = remaining;
        }
    }
}