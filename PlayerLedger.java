import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class PlayerLedger {
    
    // File paths as constants
    private static final Path LAST_PLAYER_PATH = Paths.get("Program_Files/lastplayer.log");
    private static final Path LAST_PLAYER_LEDGER_PATH = Paths.get("Program_Files/lastplayerledger.log");
    private static final Path LEDGER_FINAL_PATH = Paths.get("Program_Files/ledger_final.log");
    
    // Constants for processing
    private static final int SUBSTRING_INDEX = 49;
    
    /**
     * Constructor that initiates the player ledger processing
     * @throws IOException if file operations fail
     */
    public PlayerLedger() throws IOException {
        processPlayerLedger();
    }
    
    /**
     * Main method to process player ledger data
     * @throws IOException if file operations fail
     */
    private void processPlayerLedger() throws IOException {
        File lastPlayerFile = new File(LAST_PLAYER_PATH.toString());
        
        if (!lastPlayerFile.exists() || lastPlayerFile.length() == 0) {
            // File doesn't exist or is empty, nothing to process
            return;
        }
        
        try {
            clearOutputFile();
            processPlayerFile();
        } catch (IOException e) {
            System.err.println("Error processing player ledger: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Clears the output ledger file
     * @throws IOException if file operations fail
     */
    private void clearOutputFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(LAST_PLAYER_LEDGER_PATH.toFile())) {
            // File is cleared by opening and closing
        }
    }
    
    /**
     * Processes the player file by extracting player names and writing to ledger files
     * @throws IOException if file operations fail
     */
    private void processPlayerFile() throws IOException {
        try (Stream<String> lines = Files.lines(LAST_PLAYER_PATH, StandardCharsets.UTF_8)) {
            lines.map(this::extractPlayerName)
                 .filter(this::isValidPlayerName)
                 .forEach(this::writeToLedgerFiles);
        } catch (Exception e) {
            System.err.println("Error processing player file: " + e.getMessage());
            throw new IOException("Failed to process player file", e);
        }
    }
    
    /**
     * Extracts player name from a line by removing the first 49 characters
     * @param line the input line
     * @return the extracted player name, or empty string if extraction fails
     */
    private String extractPlayerName(String line) {
        try {
            if (line.length() > SUBSTRING_INDEX) {
                return line.substring(SUBSTRING_INDEX);
            }
        } catch (StringIndexOutOfBoundsException e) {
            // Log but don't crash - return empty string to be filtered out
            System.err.println("Warning: Line too short to extract player name: " + line);
        }
        return "";
    }
    
    /**
     * Validates if the extracted player name is valid (starts with whitespace)
     * @param playerName the player name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPlayerName(String playerName) {
        return !playerName.isEmpty() && 
               playerName.length() > 0 && 
               Character.isWhitespace(playerName.charAt(0));
    }
    
    /**
     * Writes the player name to both ledger files
     * @param playerName the player name to write
     */
    private void writeToLedgerFiles(String playerName) {
        try {
            byte[] playerNameBytes = playerName.getBytes(StandardCharsets.UTF_8);
            
            // Write to both output files
            Files.write(LAST_PLAYER_LEDGER_PATH, playerNameBytes, 
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.write(LEDGER_FINAL_PATH, playerNameBytes, 
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                       
        } catch (IOException e) {
            System.err.println("Error writing player name to ledger files: " + e.getMessage());
        }
    }
    
    /**
     * Public method for external callers who want to handle exceptions themselves
     * @throws IOException if file operations fail
     */
    public void processWithExceptions() throws IOException {
        processPlayerLedger();
    }
    
    /**
     * Utility method to check if the source file exists and has content
     * @return true if the file exists and has content, false otherwise
     */
    public boolean hasValidSourceFile() {
        File sourceFile = new File(LAST_PLAYER_PATH.toString());
        return sourceFile.exists() && sourceFile.length() > 0;
    }
}