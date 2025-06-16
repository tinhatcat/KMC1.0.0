import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PlayerInfoHashGenerator {
    
    public PlayerInfoHashGenerator() {
        generatePlayerInfoHash();
    }
    
    public void generatePlayerInfoHash() {
        Path inputFile = Paths.get("ledger_KMC/player_info_unformatted.log");
        Path outputFile = Paths.get("ledger_KMC/player_info_HASH.log");
        
        try {
            String fileContent = Files.readString(inputFile);
            String doubleHash = generateDoubleHash(fileContent);
            Files.writeString(outputFile, doubleHash, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File operation failed: " + e.getMessage());
        }
    }
    
    private String generateDoubleHash(String input) throws NoSuchAlgorithmException {
        String firstHash = calculateSHA256Hash(input);
        return calculateSHA256Hash(firstHash);
    }
    
    private String calculateSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHexString(hashBytes);
    }
    
    private String bytesToHexString(byte[] hashBytes) {
        BigInteger number = new BigInteger(1, hashBytes);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        
        // Pad with leading zeros to ensure 64-character length
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        
        return hexString.toString();
    }
}