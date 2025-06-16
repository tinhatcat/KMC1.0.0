import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintHash {
    
    private static final Path PRIVATE_KEY_PATH = Paths.get("privatekey.txt");
    
    /**
     * Constructor that processes the private key file with double SHA-256 hashing
     */
    public PrintHash() {
        processPrivateKey();
    }
    
    /**
     * Processes the private key file by double-hashing its content and overwriting the file
     */
    public void processPrivateKey() {
        try {
            String privateKeyContent = Files.readString(PRIVATE_KEY_PATH);
            String doubleHashedKey = doubleHashSHA256(privateKeyContent);
            Files.writeString(PRIVATE_KEY_PATH, doubleHashedKey, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error processing private key file: " + e.getMessage());
        }
    }
    
    /**
     * Performs double SHA-256 hashing on the input string
     * @param input the string to hash
     * @return double SHA-256 hash as hexadecimal string
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     * @throws IOException if encoding fails
     */
    public static String doubleHashSHA256(String input) throws NoSuchAlgorithmException, IOException {
        String firstHash = toHexString(getSHA256(input));
        return toHexString(getSHA256(firstHash));
    }
    
    /**
     * Generates SHA-256 hash of the input string
     * @param input string to be hashed
     * @return byte array containing the hash
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    public static byte[] getSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Converts byte array hash to hexadecimal string representation
     * @param hash byte array to convert
     * @return hexadecimal string representation of the hash (64 characters)
     */
    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        
        // Pad with leading zeros to ensure 64-character length for SHA-256
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        
        return hexString.toString();
    }
    
    /**
     * Alternative method for external callers who want to handle exceptions themselves
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     * @throws IOException if file operations fail
     */
    public void processPrivateKeyWithExceptions() throws NoSuchAlgorithmException, IOException {
        String privateKeyContent = Files.readString(PRIVATE_KEY_PATH);
        String doubleHashedKey = doubleHashSHA256(privateKeyContent);
        Files.writeString(PRIVATE_KEY_PATH, doubleHashedKey, StandardCharsets.UTF_8);
    }
}