import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/**
 * MinecraftLogProcessor - Processes Minecraft log files to extract and filter
 * blockchain-related chat messages and block information.
 */
public class MinecraftLogProcessor {
    
    // File path constants
    private static final String LATEST_COPY_A = "latestcopyA.log";
    private static final String LATEST_COPY = "latestcopy.log";
    private static final String NEXT_BLOCK_LINES = "nextBlockLines.log";
    private static final String MINECRAFT_LATEST_LOG = "/AppData/Roaming/.minecraft/logs/latest.log";
    
    // Chat detection constants
    private static final String CHAT_MARKER = " [Render thread/INFO]: [System] [CHAT] ";
    private static final int EXPECTED_PERIOD_COUNT = 6;
    private static final int TIMESTAMP_LENGTH = 10;
    
    // Transaction marker characters
    private static final String[] TRANSACTION_MARKERS = {"$", "~", "%", ";", ",", "_", "&"};
    
    // Unicode cleanup pattern
    private static final String INVALID_CHAR_PATTERN = "\\ufffd";
    
    // File paths
    private final Path latestCopyAPath;
    private final Path latestCopyPath;
    private final Path nextBlockLinesPath;
    
    public MinecraftLogProcessor() throws IOException {
        this.latestCopyAPath = Paths.get(LATEST_COPY_A);
        this.latestCopyPath = Paths.get(LATEST_COPY);
        this.nextBlockLinesPath = Paths.get(NEXT_BLOCK_LINES);
        
        processAllLogs();
    }
    
    /**
     * Main processing method that orchestrates all log processing steps
     */
    private void processAllLogs() throws IOException {
        copyNextBlockLinesToLatestA();
        copyMinecraftLogsToLatestA();
        filterAndCategorizeLines();
    }
    
    /**
     * Step 1: Copy content from nextBlockLines.log to latestcopyA.log with cleanup
     */
    private void copyNextBlockLinesToLatestA() throws IOException {
        if (!Files.exists(nextBlockLinesPath)) {
            System.out.println("nextBlockLines.log does not exist, skipping step 1");
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(nextBlockLinesPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(latestCopyAPath, 
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = cleanLine(line);
                writer.write(cleanedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error in step 1 - copying nextBlockLines: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Step 2: Copy Minecraft logs to latestcopyA.log and clear the original
     */
    private void copyMinecraftLogsToLatestA() throws IOException {
        String userHome = System.getProperty("user.home");
        Path minecraftLogPath = Paths.get(userHome + MINECRAFT_LATEST_LOG);
        
        // Clear nextBlockLines.log first
        clearFile(nextBlockLinesPath);
        
        if (!Files.exists(minecraftLogPath)) {
            System.out.println("Minecraft log file does not exist: " + minecraftLogPath);
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(minecraftLogPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(latestCopyAPath, 
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = cleanLine(line);
                writer.write(cleanedLine);
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error in step 2 - copying Minecraft logs: " + e.getMessage());
            throw e;
        } finally {
            // Clear the Minecraft log file after processing
            clearFile(minecraftLogPath);
        }
    }
    
    /**
     * Step 3: Filter and categorize lines from latestcopyA.log into appropriate output files
     */
    private void filterAndCategorizeLines() throws IOException {
        // Clear the output file first
        clearFile(latestCopyPath);
        
        if (!Files.exists(latestCopyAPath)) {
            System.out.println("latestcopyA.log does not exist, cannot filter lines");
            return;
        }
        
        int chatBlockCount = 0;
        
        try (BufferedReader reader = Files.newBufferedReader(latestCopyAPath, StandardCharsets.UTF_8);
             BufferedWriter regularWriter = Files.newBufferedWriter(latestCopyPath, 
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
             BufferedWriter blockWriter = Files.newBufferedWriter(nextBlockLinesPath, 
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = cleanLine(line);
                LineClassification classification = classifyLine(cleanedLine);
                
                // Count chat blocks
                if (classification.isChatBlock) {
                    chatBlockCount++;
                }
                
                // Route lines to appropriate files
                if (classification.isTransaction && chatBlockCount >= 0) {
                    // Transaction lines go to nextBlockLines.log
                    blockWriter.write(cleanedLine);
                    blockWriter.newLine();
                } else if (chatBlockCount == 0) {
                    // Before any chat blocks detected, write to regular file
                    regularWriter.write(cleanedLine);
                    regularWriter.newLine();
                } else {
                    // After chat blocks detected but not a transaction, write to regular file
                    regularWriter.write(cleanedLine);
                    regularWriter.newLine();
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error in step 3 - filtering and categorizing: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Classify a line to determine its type and routing
     */
    private LineClassification classifyLine(String line) {
        LineClassification classification = new LineClassification();
        
        if (line.length() > TIMESTAMP_LENGTH) {
            String withoutTimestamp = line.substring(TIMESTAMP_LENGTH);
            classification.isChatBlock = isChatBlockLine(line, withoutTimestamp);
        }
        
        classification.isTransaction = containsAllTransactionMarkers(line);
        
        return classification;
    }
    
    /**
     * Check if a line represents a chat block based on period count and chat marker
     */
    private boolean isChatBlockLine(String line, String withoutTimestamp) {
        int periodCount = countCharOccurrences(line, '.');
        return periodCount == EXPECTED_PERIOD_COUNT && withoutTimestamp.startsWith(CHAT_MARKER);
    }
    
    /**
     * Check if a line contains all required transaction markers
     */
    private boolean containsAllTransactionMarkers(String line) {
        for (String marker : TRANSACTION_MARKERS) {
            if (!line.contains(marker)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Count occurrences of a specific character in a string
     */
    private int countCharOccurrences(String text, char targetChar) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == targetChar) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Clean a line by trimming whitespace and removing invalid Unicode characters
     */
    private String cleanLine(String line) {
        if (line == null) {
            return "";
        }
        return line.trim().replaceAll(INVALID_CHAR_PATTERN, "");
    }
    
    /**
     * Clear a file by truncating its content
     */
    private void clearFile(Path filePath) throws IOException {
        try {
            Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).close();
        } catch (IOException e) {
            System.err.println("Warning: Could not clear file " + filePath + ": " + e.getMessage());
            // Don't re-throw as file clearing might not be critical in some cases
        }
    }
    
    /**
     * Get processing statistics for debugging
     */
    public void printProcessingStats() {
        System.out.println("=== Processing Statistics ===");
        printFileStats(LATEST_COPY_A, "Latest Copy A");
        printFileStats(LATEST_COPY, "Latest Copy");
        printFileStats(NEXT_BLOCK_LINES, "Next Block Lines");
    }
    
    /**
     * Print statistics for a specific file
     */
    private void printFileStats(String fileName, String displayName) {
        Path filePath = Paths.get(fileName);
        try {
            if (Files.exists(filePath)) {
                long size = Files.size(filePath);
                long lineCount = Files.lines(filePath).count();
                System.out.printf("%s: %d bytes, %d lines%n", displayName, size, lineCount);
            } else {
                System.out.printf("%s: File does not exist%n", displayName);
            }
        } catch (IOException e) {
            System.err.printf("Error getting stats for %s: %s%n", displayName, e.getMessage());
        }
    }
    
    /**
     * Inner class to hold line classification results
     */
    private static class LineClassification {
        boolean isChatBlock = false;
        boolean isTransaction = false;
    }
    
    /**
     * Main method for testing
     */
    //public static void main(String[] args) {
        //try {
            //MinecraftLogProcessor processor = new MinecraftLogProcessor();
            //System.out.println("Minecraft log processing completed successfully.");
            //processor.printProcessingStats();
        //} catch (IOException e) {
            //System.err.println("Error processing Minecraft logs: " + e.getMessage());
            //e.printStackTrace();
        //}
    //}
}