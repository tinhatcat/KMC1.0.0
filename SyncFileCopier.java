import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/**
 * SyncFileCopier - Copies content from sync file to latest copy file
 * and performs cleanup operations on both source and destination files.
 */
public class SyncFileCopier {
    
    // File path constants
    private static final String SOURCE_FILE = "Program_Files/syncCopy2.log";
    private static final String DESTINATION_FILE = "latestcopyA.log";
    
    // Character replacement pattern
    private static final String INVALID_CHAR_PATTERN = "\\ufffd";
    private static final String REPLACEMENT = "";
    
    /**
     * Constructor that initiates the file copying and cleanup process
     */
    public SyncFileCopier() throws IOException {
        copyAndCleanFiles();
        clearSourceFile();
    }
    
    /**
     * Copy content from source file to destination file with character cleanup
     */
    private void copyAndCleanFiles() throws IOException {
        Path sourcePath = Paths.get(SOURCE_FILE);
        Path destinationPath = Paths.get(DESTINATION_FILE);
        
        // Clear destination file first
        clearFile(destinationPath);
        
        // Check if source file exists
        if (!Files.exists(sourcePath)) {
            System.out.println("Source file does not exist: " + SOURCE_FILE);
            return;
        }
        
        // Copy file content with cleanup
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(destinationPath, StandardCharsets.UTF_8,
                 StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String cleanedLine = cleanLine(line);
                writer.write(cleanedLine);
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error copying file from " + SOURCE_FILE + " to " + DESTINATION_FILE + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Clear the source file after successful copy operation
     */
    private void clearSourceFile() throws IOException {
        clearFile(Paths.get(SOURCE_FILE));
    }
    
    /**
     * Clean a line by removing invalid Unicode characters
     */
    private String cleanLine(String line) {
        if (line == null) {
            return "";
        }
        return line.replaceAll(INVALID_CHAR_PATTERN, REPLACEMENT);
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
            // Don't re-throw here as file clearing might not be critical
        }
    }
    
    /**
     * Alternative method using Files.copy for simple file copying without content modification
     * This method is more efficient but doesn't perform character cleanup
     */
    public void copyFileSimple() throws IOException {
        Path sourcePath = Paths.get(SOURCE_FILE);
        Path destinationPath = Paths.get(DESTINATION_FILE);
        
        if (Files.exists(sourcePath)) {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            clearFile(sourcePath);
        }
    }
    
    /**
     * Get file information for debugging purposes
     */
    public void printFileInfo() {
        Path sourcePath = Paths.get(SOURCE_FILE);
        Path destinationPath = Paths.get(DESTINATION_FILE);
        
        System.out.println("Source file exists: " + Files.exists(sourcePath));
        System.out.println("Destination file exists: " + Files.exists(destinationPath));
        
        try {
            if (Files.exists(sourcePath)) {
                System.out.println("Source file size: " + Files.size(sourcePath) + " bytes");
            }
            if (Files.exists(destinationPath)) {
                System.out.println("Destination file size: " + Files.size(destinationPath) + " bytes");
            }
        } catch (IOException e) {
            System.err.println("Error getting file information: " + e.getMessage());
        }
    }
    
    /**
     * Main method for testing the file copier
     */
    //public static void main(String[] args) {
        //try {
            //SyncFileCopier copier = new SyncFileCopier();
            //System.out.println("File copying completed successfully.");
            //copier.printFileInfo();
        //} catch (IOException e) {
            //System.err.println("Error during file copying: " + e.getMessage());
            //e.printStackTrace();
        //}
    //}
}