import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LedgerCurrentHashProcessor {
    
    // File paths
    private static final String LEDGER_CURRENT_INPUT = "ledger_KMC/ledger_current.txt";
    private static final String LEDGER_CURRENT_OUTPUT = "ledger_KMC/ledger_current_HASH.log";
    
    public LedgerCurrentHashProcessor() {
        processLedgerCurrentHash();
    }
    
    /**
     * Processes the current ledger file by generating a double hash and writing to output file
     */
    public void processLedgerCurrentHash() {
        try {
            String ledgerContent = readLedgerCurrentContent();
            String doubleHash = generateDoubleHash(ledgerContent);
            writeLedgerCurrentHash(doubleHash);
            
        } catch (Exception e) {
            System.err.println("Error processing ledger current hash: " + e.getMessage());
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
     * Reads the current ledger content from input file
     */
    private String readLedgerCurrentContent() throws IOException {
        Path inputPath = Paths.get(LEDGER_CURRENT_INPUT);
        return Files.readString(inputPath, StandardCharsets.UTF_8);
    }
    
    /**
     * Writes the processed hash to the output file
     */
    private void writeLedgerCurrentHash(String hash) throws IOException {
        Path outputPath = Paths.get(LEDGER_CURRENT_OUTPUT);
        Files.writeString(outputPath, hash, StandardCharsets.UTF_8);
    }
}