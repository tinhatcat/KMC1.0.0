import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Synchronizes and processes Minecraft log files.
 * Copies the latest Minecraft log, processes it to remove chat messages with timestamps,
 * and cleans up temporary files.
 */
public class MinecraftLogSynchronizer {
    
    private static final String PROGRAM_FILES_DIR = "Program_Files";
    private static final String TEMP_SYNC_FILE = PROGRAM_FILES_DIR + "/syncCopy.log";
    private static final String FINAL_SYNC_FILE = PROGRAM_FILES_DIR + "/syncCopy2.log";
    private static final String MINECRAFT_LOG_PATH = "/AppData/Roaming/.minecraft/logs/latest.log";
    private static final String CHAT_BLOCK_FILTER = " [Render thread/INFO]: [System] [CHAT] ";
    private static final String REPLACEMENT_CHAR = "\\ufffd";
    private static final String PERIOD = ".";
    private static final int EXPECTED_PERIOD_COUNT = 6;
    private static final int TIME_PREFIX_LENGTH = 10;
    
    private final Path tempSyncFile;
    private final Path finalSyncFile;
    private final String minecraftLogPath;
    
    public MinecraftLogSynchronizer() throws IOException {
        this.tempSyncFile = Paths.get(TEMP_SYNC_FILE);
        this.finalSyncFile = Paths.get(FINAL_SYNC_FILE);
        this.minecraftLogPath = System.getProperty("user.home") + MINECRAFT_LOG_PATH;
        
        // Ensure program files directory exists
        Files.createDirectories(Paths.get(PROGRAM_FILES_DIR));
        
        copyMinecraftLog();
        processAndFilterLog();
    }
    
    /**
     * Copies the Minecraft latest.log file to a temporary sync file.
     * Removes replacement characters and clears the original log file.
     * 
     * @throws IOException if file operations fail
     */
    private void copyMinecraftLog() throws IOException {
        Path minecraftLog = Paths.get(minecraftLogPath);
        
        if (!Files.exists(minecraftLog)) {
            throw new FileNotFoundException("Minecraft log file not found: " + minecraftLogPath);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(minecraftLog);
             BufferedWriter writer = Files.newBufferedWriter(tempSyncFile, 
                 java.nio.file.StandardOpenOption.CREATE, 
                 java.nio.file.StandardOpenOption.APPEND)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = line.trim().replaceAll(REPLACEMENT_CHAR, "");
                writer.write(cleanedLine);
                writer.newLine();
            }
        }
        
        // Clear the original Minecraft log file
        clearFile(minecraftLog);
    }
    
    /**
     * Processes the temporary sync file and filters out chat messages.
     * Chat messages are identified by having exactly 6 periods and containing the chat block filter.
     * 
     * @throws IOException if file operations fail
     */
    private void processAndFilterLog() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(tempSyncFile);
             BufferedWriter writer = Files.newBufferedWriter(finalSyncFile, 
                 java.nio.file.StandardOpenOption.CREATE, 
                 java.nio.file.StandardOpenOption.APPEND)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                
                if (shouldFilterLine(trimmedLine)) {
                    // Clear the final sync file when chat message is detected
                    clearFile(finalSyncFile);
                    break;
                } else {
                    String cleanedLine = trimmedLine.replaceAll(REPLACEMENT_CHAR, "");
                    writer.write(cleanedLine);
                    writer.newLine();
                }
            }
        }
        
        // Clean up temporary file
        clearFile(tempSyncFile);
    }
    
    /**
     * Determines if a line should be filtered out (is a chat message with timestamp).
     * 
     * @param line the line to check
     * @return true if the line should be filtered out
     */
    private boolean shouldFilterLine(String line) {
        if (line.length() <= TIME_PREFIX_LENGTH) {
            return false;
        }
        
        // Count periods to identify timestamp format
        int periodCount = countOccurrences(line, PERIOD);
        
        if (periodCount == EXPECTED_PERIOD_COUNT) {
            String contentAfterTime = line.substring(TIME_PREFIX_LENGTH);
            return contentAfterTime.startsWith(CHAT_BLOCK_FILTER);
        }
        
        return false;
    }
    
    /**
     * Counts the occurrences of a substring in a string.
     * 
     * @param text the text to search in
     * @param substring the substring to count
     * @return the number of occurrences
     */
    private int countOccurrences(String text, String substring) {
        return text.length() - text.replace(substring, "").length();
    }
    
    /**
     * Clears the contents of a file by overwriting it with an empty file.
     * 
     * @param filePath the path of the file to clear
     * @throws IOException if the file operation fails
     */
    private void clearFile(Path filePath) throws IOException {
        Files.write(filePath, new byte[0]);
    }
    
    //public static void main(String[] args) {
        //try {
            //new MinecraftLogSynchronizer();
            //System.out.println("Minecraft log synchronization completed successfully.");
        //} catch (IOException e) {
            //System.err.println("Error during log synchronization: " + e.getMessage());
            //e.printStackTrace();
        //}
    //}
}
