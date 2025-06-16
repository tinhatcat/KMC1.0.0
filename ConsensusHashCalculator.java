import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConsensusHashCalculator {
    
    // File paths
    private static final String LEDGER_HASHES_FILE = "ledger_KMC/ledgerhashes_HASH.log";
    private static final String PLAYER_INFO_FILE = "ledger_KMC/player_info_HASH.log";
    private static final String LEDGER_CURRENT_FILE = "ledger_KMC/ledger_current_HASH.log";
    private static final String CONSENSUS_FILE = "ledger_KMC/consensus_HASH.log";
    
    public ConsensusHashCalculator() {
        calculateConsensusHash();
    }
    
    /**
     * Calculates the consensus hash from ledger files and writes it to the consensus file
     */
    public void calculateConsensusHash() {
        try {
            String ledgerHashesContent = readFileContent(LEDGER_HASHES_FILE);
            String playerInfoContent = readFileContent(PLAYER_INFO_FILE);
            String ledgerCurrentContent = readFileContent(LEDGER_CURRENT_FILE);
            
            String combinedContent = ledgerHashesContent + playerInfoContent + ledgerCurrentContent;
            String consensusHash = generateDoubleHash(combinedContent);
            
            writeConsensusHash(consensusHash);
            
        } catch (Exception e) {
            System.err.println("Error calculating consensus hash: " + e.getMessage());
        }
    }
    
    /**
     * Generates a SHA-256 hash of the input string
     */
    public static String generateSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHexString(hashBytes);
    }
    
    /**
     * Converts byte array to hexadecimal string with proper padding
     */
    public static String bytesToHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        
        // Pad with leading zeros to ensure 64-character length
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        
        return hexString.toString();
    }
    
    /**
     * Generates a double SHA-256 hash (hash of hash)
     */
    private String generateDoubleHash(String input) throws NoSuchAlgorithmException {
        String firstHash = generateSHA256Hash(input);
        return generateSHA256Hash(firstHash);
    }
    
    /**
     * Reads content from a file
     */
    private String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
    
    /**
     * Writes the consensus hash to the consensus file
     */
    private void writeConsensusHash(String consensusHash) throws IOException {
        Path consensusPath = Paths.get(CONSENSUS_FILE);
        Files.writeString(consensusPath, consensusHash, StandardCharsets.UTF_8);
    }
}