import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Processes text files by extracting content after specific delimiters.
 * Provides functionality to copy files and extract text following ">" markers.
 */
public class TxtCopier {
    
    private static final String SOURCE_FILE = "Program_Files/latestTxs4.log";
    private static final String DESTINATION_FILE = "discordC.txt";
    private static final String DELIMITER = "> ";
    private static final String PROGRAM_FILES_DIR = "Program_Files";
    
    private final Path sourceFile;
    private final Path destinationFile;
    
    public TxtCopier() throws IOException {
        this.sourceFile = Paths.get(SOURCE_FILE);
        this.destinationFile = Paths.get(DESTINATION_FILE);
        
        // Ensure program files directory exists
        Files.createDirectories(Paths.get(PROGRAM_FILES_DIR));
        
        processTextFile();
    }
    
    /**
     * Processes the source file by extracting text after the delimiter and writing to destination.
     * Each line is processed to find content after "> " and written with a space prefix.
     * 
     * @throws IOException if file operations fail
     */
    private void processTextFile() throws IOException {
        if (!Files.exists(sourceFile)) {
            throw new FileNotFoundException("Source file not found: " + SOURCE_FILE);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(sourceFile);
             BufferedWriter writer = Files.newBufferedWriter(destinationFile)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String processedLine = extractContentAfterDelimiter(line);
                if (processedLine != null) {
                    writer.write(" " + processedLine);
                }
            }
        }
    }
    
    /**
     * Extracts content from a line after the specified delimiter.
     * 
     * @param line the line to process
     * @return the content after the delimiter, or null if delimiter not found
     */
    private String extractContentAfterDelimiter(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        
        int delimiterIndex = line.indexOf(DELIMITER);
        if (delimiterIndex == -1) {
            return null; // Delimiter not found
        }
        
        return line.substring(delimiterIndex + DELIMITER.length());
    }
    
    /**
     * Simple file copy operation using NIO.
     * 
     * @throws IOException if copy operation fails
     */
    public void copyFile() throws IOException {
        Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Alternative processing method that extracts content after single ">" character.
     * This method processes each line to find content after ">" (without space).
     * 
     * @throws IOException if file operations fail
     */
    public void alternativeProcessing() throws IOException {
        final String singleDelimiter = ">";
        
        try (BufferedReader reader = Files.newBufferedReader(sourceFile);
             BufferedWriter writer = Files.newBufferedWriter(destinationFile)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                int delimiterIndex = line.indexOf(singleDelimiter);
                if (delimiterIndex != -1) {
                    String extractedContent = line.substring(delimiterIndex + 1);
                    writer.write(extractedContent);
                }
            }
        }
    }
    
    /**
     * Static utility method for processing text files with custom source and destination.
     * 
     * @param sourceFilePath path to the source file
     * @param destinationFilePath path to the destination file
     * @throws IOException if file operations fail
     */
    public static void processFile(String sourceFilePath, String destinationFilePath) throws IOException {
        Path source = Paths.get(sourceFilePath);
        Path destination = Paths.get(destinationFilePath);
        
        if (!Files.exists(source)) {
            throw new FileNotFoundException("Source file not found: " + sourceFilePath);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(source);
             BufferedWriter writer = Files.newBufferedWriter(destination)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                int delimiterIndex = line.indexOf(DELIMITER);
                if (delimiterIndex != -1) {
                    String extractedContent = line.substring(delimiterIndex + DELIMITER.length());
                    writer.write(" " + extractedContent);
                }
            }
        }
    }
    
    //public static void main(String[] args) {
        //try {
            //TxtCopier processor = new TxtCopier();
            //System.out.println("Text processing completed successfully.");
            
            // Example of using the static utility method
            // processFile("custom_source.txt", "custom_destination.txt");
            
        //} catch (IOException e) {
            //System.err.println("Error during text processing: " + e.getMessage());
            //e.printStackTrace();
        //}
    //}
}