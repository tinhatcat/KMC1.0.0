import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class PlayerExtractor {
    private static final String INPUT_FILE = "latestcopy.log";
    private static final String OUTPUT_ALL_PLAYERS = "Program_Files/latestplayers.log";
    private static final String OUTPUT_LAST_PLAYER = "Program_Files/lastplayer.log";
    
    // Pattern positions for player identification
    private static final int WHITESPACE_POS_1 = 41;
    private static final int UPPERCASE_POS_1 = 44;
    private static final int UPPERCASE_POS_2 = 45;
    private static final int UPPERCASE_POS_3 = 46;
    private static final int WHITESPACE_POS_2 = 48;
    private static final int SPACE_POS = 49;
    
    public PlayerExtractor() throws IOException {
        extractPlayers();
    }
    
    /**
     * Extract player information from the input file and write to output files
     */
    private void extractPlayers() throws IOException {
        Path inputPath = Paths.get(INPUT_FILE);
        
        if (!Files.exists(inputPath)) {
            System.err.println("Input file not found: " + INPUT_FILE);
            return;
        }
        
        // Clear the last player file at the start
        clearFile(OUTPUT_LAST_PLAYER);
        
        try (Stream<String> lines = Files.lines(inputPath)) {
            lines.map(String::trim)
                 .filter(this::isPlayerLine)
                 .forEach(this::writePlayerData);
        }
    }
    
    /**
     * Check if a line matches the player pattern
     */
    private boolean isPlayerLine(String line) {
        try {
            return line.length() > SPACE_POS &&
                   Character.isWhitespace(line.charAt(WHITESPACE_POS_1)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_1)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_2)) &&
                   Character.isUpperCase(line.charAt(UPPERCASE_POS_3)) &&
                   Character.isWhitespace(line.charAt(WHITESPACE_POS_2)) &&
                   Character.isSpaceChar(line.charAt(SPACE_POS));
        } catch (StringIndexOutOfBoundsException e) {
            // Line is too short to match pattern
            return false;
        }
    }
    
    /**
     * Write player data to both output files
     */
    private void writePlayerData(String line) {
        try {
            // Append to all players file with line separator
            writeToFile(OUTPUT_ALL_PLAYERS, System.lineSeparator() + line, true);
            
            // Overwrite last player file
            writeToFile(OUTPUT_LAST_PLAYER, line, false);
            
        } catch (IOException e) {
            System.err.println("Error writing player data: " + e.getMessage());
        }
    }
    
    /**
     * Write content to a file
     */
    private void writeToFile(String filePath, String content, boolean append) throws IOException {
        Path path = Paths.get(filePath);
        
        // Ensure parent directory exists
        Files.createDirectories(path.getParent());
        
        if (append) {
            Files.write(path, content.getBytes(), 
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            Files.write(path, content.getBytes(), 
                       StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
    
    /**
     * Clear the contents of a file
     */
    private void clearFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[0], 
                   StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}