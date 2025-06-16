import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Block Parser - Extracts and filters blockchain blocks from log files
 * based on specific formatting patterns.
 */
public class BlockParser {
    
    // File path constants
    private static final String INPUT_FILE = "latestcopy.log";
    private static final String LATEST_BLOCKS_OUTPUT = "Program_Files/latestblocks.log";
    private static final String LATEST_BLOCK_OUTPUT = "Program_Files/latestblock.log";
    private static final String LAST_BLOCK_OUTPUT = "Program_Files/lastblock.log";
    
    // Block format validation positions
    private static final int WHITESPACE_POS_1 = 41;
    private static final int UPPERCASE_POS_1 = 44;
    private static final int UPPERCASE_POS_2 = 45;
    private static final int UPPERCASE_POS_3 = 46;
    private static final int WHITESPACE_POS_2 = 48;
    private static final int DIGIT_POS = 49;
    private static final int MIN_LINE_LENGTH = 50;
    
    public BlockParser() throws IOException {
        parseBlocks();
    }
    
    /**
     * Parses blocks from the input file and writes valid blocks to output files.
     */
    public void parseBlocks() throws IOException {
        initializeOutputFiles();
        
        Path inputPath = Paths.get(INPUT_FILE);
        if (!Files.exists(inputPath)) {
            System.err.println("Input file does not exist: " + INPUT_FILE);
            return;
        }
        
        try (Stream<String> lines = Files.lines(inputPath, StandardCharsets.UTF_8)) {
            lines.map(String::trim)
                 .filter(this::isValidBlockFormat)
                 .forEach(this::writeBlockToFiles);
            
            //System.out.println("Block parsing completed successfully");
        } catch (IOException e) {
            System.err.println("Error processing blocks: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initializes output files by clearing the latest block file.
     */
    private void initializeOutputFiles() throws IOException {
        try {
            // Clear the latest block file
            Files.deleteIfExists(Paths.get(LATEST_BLOCK_OUTPUT));
            Files.createFile(Paths.get(LATEST_BLOCK_OUTPUT));
        } catch (IOException e) {
            System.err.println("Error initializing output files: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validates if a line matches the expected block format pattern.
     * 
     * @param line The line to validate
     * @return true if the line matches the block format, false otherwise
     */
    private boolean isValidBlockFormat(String line) {
        try {
            // Check minimum length requirement
            if (line.length() < MIN_LINE_LENGTH) {
                return false;
            }
            
            // Validate specific character patterns
            return Character.isWhitespace(line.charAt(WHITESPACE_POS_1)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_1)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_2)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_3)) &&
                   Character.isWhitespace(line.charAt(WHITESPACE_POS_2)) &&
                   Character.isDigit(line.charAt(DIGIT_POS)) &&
                   line.contains(".");
                   
        } catch (StringIndexOutOfBoundsException e) {
            // Line doesn't meet minimum length requirements
            return false;
        }
    }
    
    /**
     * Writes a valid block line to all output files.
     * 
     * @param blockLine The validated block line to write
     */
    private void writeBlockToFiles(String blockLine) {
        try {
            // Write to latest blocks (with line separator)
            writeToFile(LATEST_BLOCKS_OUTPUT, System.lineSeparator() + blockLine, true);
            
            // Write to other files (without line separator)
            writeToFile(LAST_BLOCK_OUTPUT, blockLine, true);
            writeToFile(LATEST_BLOCK_OUTPUT, blockLine, true);
            
        } catch (IOException e) {
            System.err.println("Error writing block to files: " + e.getMessage());
        }
    }
    
    /**
     * Writes content to a file with specified options.
     * 
     * @param filePath The path of the file to write to
     * @param content The content to write
     * @param append Whether to append to the file or overwrite
     */
    private void writeToFile(String filePath, String content, boolean append) throws IOException {
        Path path = Paths.get(filePath);
        
        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());
        
        StandardOpenOption[] options = append ? 
            new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND} :
            new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
            
        Files.write(path, content.getBytes(StandardCharsets.UTF_8), options);
    }
    
    /**
     * Utility method to get block format requirements as a string for debugging.
     */
    public String getFormatRequirements() {
        return String.format(
            "Block format requirements:\n" +
            "- Minimum length: %d characters\n" +
            "- Position %d: whitespace\n" +
            "- Positions %d-%d: uppercase letters\n" +
            "- Position %d: whitespace\n" +
            "- Position %d: digit\n" +
            "- Must contain: '.'",
            MIN_LINE_LENGTH, WHITESPACE_POS_1, UPPERCASE_POS_1, UPPERCASE_POS_3,
            WHITESPACE_POS_2, DIGIT_POS
        );
    }
}