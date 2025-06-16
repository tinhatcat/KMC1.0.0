import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerInfoProcessor {
    
    private static final String PLAYER_INFO_TXT = "ledger_KMC/player_info.txt";
    private static final String PLAYER_INFO_LOG = "ledger_KMC/player_info.log";
    private static final String LOCAL_PLAYER_FILE = "localplayer.txt";
    
    private static final String WALLET_ADDRESS_FILE = "wallet_address.log";
    private static final String PLAYER_BALANCE_FILE = "player_balance.log";
    private static final String PLAYER_TXS_FILE = "player_txs.log";
    
    public PlayerInfoProcessor() throws IOException {
        initializeOutputFiles();
        processPlayerInfoFile();
        extractPlayerData();
    }
    
    private void initializeOutputFiles() throws IOException {
        // Clear existing output files
        new PrintWriter(WALLET_ADDRESS_FILE).close();
        new PrintWriter(PLAYER_BALANCE_FILE).close();
        new PrintWriter(PLAYER_TXS_FILE).close();
    }
    
    private void processPlayerInfoFile() throws IOException {
        Path txtFile = Paths.get(PLAYER_INFO_TXT);
        Path logFile = Paths.get(PLAYER_INFO_LOG);
        
        if (Files.exists(txtFile)) {
            System.out.println("Converting player_info.txt to .log format...");
            
            // Delete existing log file if it exists
            if (Files.exists(logFile)) {
                Files.delete(logFile);
            }
            
            // Convert txt to log format
            convertFileFormat(PLAYER_INFO_TXT, PLAYER_INFO_LOG);
            
            // Delete the original txt file
            Files.delete(txtFile);
            System.out.println("File conversion completed successfully!");
        }
    }
    
    private void convertFileFormat(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }
    }
    
    private void extractPlayerData() throws IOException {
        Path localPlayerPath = Paths.get(LOCAL_PLAYER_FILE);
        
        if (!Files.exists(localPlayerPath)) {
            System.out.println("Local player file not found: " + LOCAL_PLAYER_FILE);
            return;
        }
        
        //String localPlayerName = Files.readString(localPlayerPath).trim();
        String localPlayerName = Files.readString(localPlayerPath);
        // Remove leading character if present
        if (localPlayerName.length() > 1) {
            localPlayerName = localPlayerName.substring(1);
        }
        
        boolean playerFound = searchAndExtractPlayerInfo(localPlayerName);
        
        if (!playerFound) {
            System.out.println("Player not found in player_info.log. Player must mine a block first.");
        }
    }
    
    private boolean searchAndExtractPlayerInfo(String targetPlayer) throws IOException {
        File playerInfoFile = new File(PLAYER_INFO_LOG);
        
        if (!playerInfoFile.exists()) {
            System.out.println("Player info log file not found: " + PLAYER_INFO_LOG);
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(playerInfoFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.equals(targetPlayer)) {
                    // Found the player, read the next 3 lines
                    String walletAddress = reader.readLine();
                    String balance = reader.readLine();
                    String transactions = reader.readLine();
                    
                    if (walletAddress != null && balance != null && transactions != null) {
                        writePlayerData(walletAddress, balance, transactions);
                        System.out.println("Player data extracted successfully for: " + targetPlayer);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private void writePlayerData(String walletAddress, String balance, String transactions) throws IOException {
        Files.writeString(Paths.get(WALLET_ADDRESS_FILE), walletAddress);
        Files.writeString(Paths.get(PLAYER_BALANCE_FILE), balance);
        Files.writeString(Paths.get(PLAYER_TXS_FILE), transactions);
    }
}