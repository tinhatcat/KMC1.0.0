import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/**
 * Discord Message Builder - Constructs blockchain messages by combining
 * block data, player information, transactions, and consensus hash.
 */
public class DiscordMessageBuilder {
    
    // File path constants
    private static final String DISCORD_OUTPUT_FILE = "discordM.txt";
    private static final String LAST_BLOCK_LEDGER = "Program_Files/lastblockledger.log";
    private static final String LAST_PLAYER_LEDGER = "Program_Files/lastplayerledger.log";
    private static final String LAST_BLOCK_HASH = "Program_Files/lastblockhash.log";
    private static final String DISCORD_CONTENT = "discordC.txt";
    private static final String CONSENSUS_HASH = "ledger_KMC/consensus_HASH.log";
    
    public DiscordMessageBuilder() throws IOException {
        buildDiscordMessage();
    }
    
    /**
     * Builds a Discord message by combining blockchain data components
     * and writes it to the output file.
     */
    public void buildDiscordMessage() throws IOException {
        try {
            MessageComponents components = loadMessageComponents();
            String message = formatMessage(components);
            writeMessageToFile(message);
            
            //System.out.println("Discord message built successfully");
        } catch (IOException e) {
            System.err.println("Error building Discord message: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Loads all required message components from their respective files.
     */
    private MessageComponents loadMessageComponents() throws IOException {
        return new MessageComponents(
            readFileContent(LAST_BLOCK_LEDGER),
            readFileContent(LAST_PLAYER_LEDGER),
            readFileContent(LAST_BLOCK_HASH),
            readFileContent(DISCORD_CONTENT),
            readFileContent(CONSENSUS_HASH)
        );
    }
    
    /**
     * Formats the message components into a single Discord message string.
     */
    private String formatMessage(MessageComponents components) {
        return String.format("%s%s%s%s =%s",
            components.blockData,
            components.playerData,
            components.hashData,
            components.transactionData,
            components.consensusHash
        );
    }
    
    /**
     * Writes the formatted message to the Discord output file.
     */
    private void writeMessageToFile(String message) throws IOException {
        try (FileWriter writer = new FileWriter(DISCORD_OUTPUT_FILE, true)) {
            writer.write(message);
        }
    }
    
    /**
     * Reads the content of a file and returns it as a string.
     * Returns empty string if file doesn't exist or can't be read.
     */
    private String readFileContent(String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                System.err.println("Warning: File does not exist: " + filePath);
                return "";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Data class to hold all message components.
     */
    private static class MessageComponents {
        final String blockData;
        final String playerData;
        final String hashData;
        final String transactionData;
        final String consensusHash;
        
        MessageComponents(String blockData, String playerData, String hashData, 
                         String transactionData, String consensusHash) {
            this.blockData = blockData != null ? blockData : "";
            this.playerData = playerData != null ? playerData : "";
            this.hashData = hashData != null ? hashData : "";
            this.transactionData = transactionData != null ? transactionData : "";
            this.consensusHash = consensusHash != null ? consensusHash : "";
        }
    }
}