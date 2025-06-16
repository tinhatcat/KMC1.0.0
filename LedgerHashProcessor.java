import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LedgerHashProcessor {
    
    // File paths
    private static final String LEDGER_HASHES_INPUT = "ledger_KMC/ledgerhashes.log";
    private static final String LEDGER_HASHES_OUTPUT = "ledger_KMC/ledgerhashes_HASH.log";
    
    public LedgerHashProcessor() {
        processLedgerHashes();
    }
    
    /**
     * Processes the ledger hashes file by generating a double hash and writing to output file
     */
    public void processLedgerHashes() {
        try {
            String ledgerContent = readLedgerContent();
            String doubleHash = generateDoubleHash(ledgerContent);
            writeLedgerHash(doubleHash);
            
        } catch (Exception e) {
            System.err.println("Error processing ledger hashes: " + e.getMessage());
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
     * Reads the ledger hashes content from input file
     */
    private String readLedgerContent() throws IOException {
        Path inputPath = Paths.get(LEDGER_HASHES_INPUT);
        return Files.readString(inputPath, StandardCharsets.UTF_8);
    }
    
    /**
     * Writes the processed hash to the output file
     */
    private void writeLedgerHash(String hash) throws IOException {
        Path outputPath = Paths.get(LEDGER_HASHES_OUTPUT);
        Files.writeString(outputPath, hash, StandardCharsets.UTF_8);
    }
}