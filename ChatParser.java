import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * Parses chat logs and extracts lines matching specific formatting criteria.
 * Filters lines where position 32 is whitespace and positions 35-37 are uppercase letters.
 */
public class ChatParser {
    
    private static final String INPUT_FILE = "latestcopy.log";
    private static final String OUTPUT_FILE = "Program_Files/latestchat.log";
    private static final int WHITESPACE_POSITION = 32;
    private static final int FIRST_UPPERCASE_POSITION = 35;
    private static final int SECOND_UPPERCASE_POSITION = 36;
    private static final int THIRD_UPPERCASE_POSITION = 37;
    
    public ChatParser() throws IOException {
        parseChat();
    }
    
    /**
     * Parses the input chat log file and writes filtered lines to output file.
     * 
     * @throws IOException if file operations fail
     */
    public void parseChat() throws IOException {
        Path inputPath = Paths.get(INPUT_FILE);
        Path outputPath = Paths.get(OUTPUT_FILE);
        
        // Ensure output directory exists
        Files.createDirectories(outputPath.getParent());
        
        try (Stream<String> lines = Files.lines(inputPath)) {
            lines.map(String::trim)
                 .filter(this::matchesChatFormat)
                 .forEach(line -> writeLineToFile(line, outputPath));
        }
    }
    
    /**
     * Checks if a line matches the expected chat format.
     * 
     * @param line the line to check
     * @return true if line matches format criteria
     */
    private boolean matchesChatFormat(String line) {
        if (line.length() <= THIRD_UPPERCASE_POSITION) {
            return false;
        }
        
        return Character.isWhitespace(line.charAt(WHITESPACE_POSITION)) &&
               Character.isUpperCase(line.charAt(FIRST_UPPERCASE_POSITION)) &&
               Character.isUpperCase(line.charAt(SECOND_UPPERCASE_POSITION)) &&
               Character.isUpperCase(line.charAt(THIRD_UPPERCASE_POSITION));
    }
    
    /**
     * Writes a single line to the output file.
     * 
     * @param line the line to write
     * @param outputPath the path to write to
     */
    private void writeLineToFile(String line, Path outputPath) {
        try {
            String lineWithSeparator = line + System.lineSeparator();
            Files.write(outputPath, 
                       lineWithSeparator.getBytes(), 
                       StandardOpenOption.CREATE, 
                       StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing line to file: " + e.getMessage());
        }
    }
    
    //public static void main(String[] args) {
        //try {
            //new ChatParser();
            //System.out.println("Chat parsing completed successfully.");
        //} catch (IOException e) {
            //System.err.println("Error during chat parsing: " + e.getMessage());
            //e.printStackTrace();
        //}
    //}
}
