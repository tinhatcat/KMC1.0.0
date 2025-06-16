import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhoIs {
    
    private final String userDirectory = System.getProperty("user.home");
    private static final int HASH_ITERATIONS = 2002;
    private static final int HASH_LENGTH = 64;
    private static final int WAIT_MAIN = 7000;
    private static final int WAIT_BLOCK = 5500;
    
    public WhoIs() throws FileNotFoundException, IOException {
        processWhoIs();
    }
    
    public synchronized void processWhoIs() throws FileNotFoundException, IOException {
        Path localPlayerFile = Paths.get("localplayer.txt");
        Path playerLedgerFile = Paths.get("Program_Files/lastplayerledger.log");
        
        try {
            String ledgerContent = Files.readString(playerLedgerFile);
            long mismatchIndex = Files.mismatch(localPlayerFile, playerLedgerFile);
            
            if (mismatchIndex == -1L) {
                try {
                    generateAndProcessHash();
                    processBlock();
                } catch (Exception e) {
                    // Silent catch as per original code
                }
            }
            
            int contentLength = ledgerContent.length();
            
            if (mismatchIndex != -1L && contentLength < 17) {
                wait(WAIT_MAIN);
                
                try {
                    processNewLines();
                    cleanChatSequence();
                    processHash();
                } catch (Exception e) {
                    // Silent catch as per original code
                }
            }
        } catch (Exception e) {
            System.err.println("Error comparing files: " + e.getMessage());
        }
        
        // Clear the lastplayer.log file
        new PrintWriter("Program_Files/lastplayer.log").close();
    }
    
    private void generateAndProcessHash() {
        Path blockLedgerPath = Paths.get("Program_Files/lastblockledger.log");
        Path blockHashPath = Paths.get("Program_Files/lastblockhash.log");
        Path privateKeyPath = Paths.get("privatekey.txt");
        
        try {
            String blockLedgerContent = Files.readString(blockLedgerPath);
            String privateKeyContent = Files.readString(privateKeyPath);
            String combinedContent = blockLedgerContent + privateKeyContent;
            
            String hashedContent = combinedContent;
            for (int i = 1; i <= HASH_ITERATIONS; i++) {
                hashedContent = toHexString(getSHA(hashedContent));
                
                if (i == HASH_ITERATIONS) {
                    Files.writeString(blockHashPath, hashedContent);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        } catch (IOException e) {
            System.out.println("Exception thrown for IO: " + e);
        }
    }
    
    private static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < HASH_LENGTH) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
    
    private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    
    private void processBlock() throws InterruptedException, AWTException, FileNotFoundException, IOException {
        Path blockHashPath = Paths.get("Program_Files/lastblockhash.log");
        String blockHashContent = Files.readString(blockHashPath);
        Pattern pattern = Pattern.compile("[a-zA-Z0-9 \t]*");
        Matcher matcher = pattern.matcher(blockHashContent);
        
        if (matcher.matches()) {
            createNewBlock();
            wait(WAIT_BLOCK);
            processNewLines();
            cleanChatSequence();
            processHash();
        } else {
            processHashAlternative();
        }
    }
    
    private synchronized void createNewBlock() throws InterruptedException, AWTException, IOException {
        wait(100);
        String blockHashContent = Files.readString(Paths.get("Program_Files/lastblockhash.log"));
        wait(200);
        
        StringSelection stringSelection = new StringSelection(blockHashContent);
        wait(200);
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        wait(200);
        clipboard.setContents(stringSelection, null);
        wait(200);
        
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_T);
        wait(10);
        robot.keyRelease(KeyEvent.VK_T);
        wait(300);
        
        robot.keyPress(KeyEvent.VK_CONTROL);
        wait(10);
        robot.keyPress(KeyEvent.VK_V);
        wait(10);
        robot.keyRelease(KeyEvent.VK_V);
        wait(10);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        wait(300);
        
        robot.keyPress(KeyEvent.VK_ENTER);
        wait(10);
        robot.keyRelease(KeyEvent.VK_ENTER);
        wait(200);
    }
    
    private void processNewLines() throws IOException {
        new PrintWriter("Program_Files/latestcopy3.log").close();
        File outputFile = new File("Program_Files/latestcopy3.log");
        
        String minecraftLogPath = userDirectory + "/AppData/Roaming/.minecraft/logs/latest.log";
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(minecraftLogPath)));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                writer.write(trimmedLine.replaceAll("\\ufffd", ""));
                writer.newLine();
            }
        }
    }
    
    private void cleanChatSequence() throws FileNotFoundException, IOException {
        cleanChatStep1();
        cleanChatStep2();
        cleanChatStep3();
        cleanChatStep4();
    }
    
    private void cleanChatStep1() throws FileNotFoundException, IOException {
        File inputFile = new File("Program_Files/latestcopy3.log");
        new PrintWriter("Program_Files/otherplayerhash.log").close();
        File outputFile = new File("Program_Files/otherplayerhash.log");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                int bracketCount = trimmedLine.length() - trimmedLine.replace("<", "").length();
                
                if (bracketCount > 1 || bracketCount == 0) {
                    continue;
                }
                
                writer.write(currentLine + System.getProperty("line.separator"));
            }
        }
    }
    
    private void cleanChatStep2() throws FileNotFoundException, IOException {
        new PrintWriter("Program_Files/otherplayerhash2.log").close();
        Path inputPath = Paths.get("Program_Files/otherplayerhash.log");
        String outputFile = "Program_Files/otherplayerhash2.log";
        
        try (Stream<String> lines = Files.lines(inputPath).map(line -> line.substring(39))) {
            lines.filter(line -> Character.isWhitespace(line.charAt(0)))
                 .forEach(line -> {
                     try {
                         Files.write(Paths.get(outputFile), 
                                   (line + System.getProperty("line.separator")).getBytes(),
                                   StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                     } catch (IOException | StringIndexOutOfBoundsException e) {
                         // Silent catch as per original code
                     }
                 });
        } catch (StringIndexOutOfBoundsException e) {
            // Silent catch as per original code
        }
    }
    
    private void cleanChatStep3() throws FileNotFoundException, IOException {
        new PrintWriter("Program_Files/otherplayerhash3.log").close();
        Path inputPath = Paths.get("Program_Files/otherplayerhash2.log");
        Path outputPath = Paths.get("Program_Files/otherplayerhash3.log");
        
        try {
            String content = Files.readString(inputPath);
            Files.writeString(outputPath, content.replaceAll("<", "").replaceAll(">", ""));
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException" + e);
        } catch (IOException e) {
            System.out.println("Exception thrown for IO: " + e);
        }
    }
    
    private void cleanChatStep4() throws FileNotFoundException, IOException {
        new PrintWriter("Program_Files/lastblockhash.log").close();
        Path playerLedgerPath = Paths.get("Program_Files/lastplayerledger.log");
        String playerLedgerContent = Files.readString(playerLedgerPath);
        
        File inputFile = new File("Program_Files/otherplayerhash3.log");
        File outputFile = new File("Program_Files/lastblockhash.log");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String currentLine = reader.readLine();
            if (currentLine != null && currentLine.startsWith(playerLedgerContent)) {
                writer.write(currentLine.replaceAll(playerLedgerContent, ""));
            }
        }
    }
    
    private void processHash() throws FileNotFoundException, IOException {
        Path blockHashPath = Paths.get("Program_Files/lastblockhash.log");
        String blockHashContent = Files.readString(blockHashPath);
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
        Matcher matcher = pattern.matcher(blockHashContent.substring(1));
        
        if (matcher.matches() && blockHashContent.length() <= 65) {
            writeFinalHash();
        } else {
            writeFinalHash();
        }
    }
    
    private void processHashAlternative() throws FileNotFoundException, IOException {
        String outputFilePath = "Program_Files/ledger_final.log";
        Path blockHashPath = Paths.get("Program_Files/lastblockhash.log");
        String blockHashContent = Files.readString(blockHashPath);
        String truncatedHash = blockHashContent.substring(0, HASH_LENGTH);
        
        System.out.println("str19a" + truncatedHash);
        
        try (FileWriter writer = new FileWriter(outputFilePath, true)) {
            writer.write(truncatedHash);
            System.out.println("str19a" + truncatedHash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeFinalHash() throws FileNotFoundException, IOException {
        String outputFilePath = "Program_Files/ledger_final.log";
        Path blockHashPath = Paths.get("Program_Files/lastblockhash.log");
        String blockHashContent = Files.readString(blockHashPath);
        String truncatedHash = blockHashContent.substring(0, HASH_LENGTH);
        
        try (FileWriter writer = new FileWriter(outputFilePath, true)) {
            writer.write(truncatedHash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}