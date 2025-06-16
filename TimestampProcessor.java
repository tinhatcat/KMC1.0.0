import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TimestampProcessor {
    private static final String INPUT_FILE = "timestamp.txt";
    private static final String OUTPUT_FILE = "timestamp1.txt";
    
    public TimestampProcessor() throws IOException {
        processTimestamp();
    }
    
    /**
     * Read the first line from timestamp.txt and append it to timestamp1.txt with a leading space
     */
    private void processTimestamp() throws IOException {
        Path inputPath = Paths.get(INPUT_FILE);
        Path outputPath = Paths.get(OUTPUT_FILE);
        
        // Check if input file exists
        if (!Files.exists(inputPath)) {
            throw new IOException("Input file not found: " + INPUT_FILE);
        }
        
        // Read the first line from the input file
        String firstLine = Files.readAllLines(inputPath).get(0);
        
        // Append the line with a leading space to the output file
        String content = " " + firstLine;
        Files.write(outputPath, content.getBytes(), 
                   StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}