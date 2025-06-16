import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 
import java.util.stream.Stream;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;


public class BlockLedger{


public BlockLedger() throws IOException {
        processBlockLedger();
    }
    
    /**
     * Main method to process block ledger
     */
    public void processBlockLedger() throws IOException {
        File blockFile = new File("Program_Files/lastblock.log");
        
        if (blockFile.exists() && blockFile.length() > 0) {
            // Clear the ledger file for new processing
            new PrintWriter("Program_Files/lastblockledger.log").close();
            processBlockFile();
        }
    }
    
    /**
     * Process the block file and extract relevant data
     */
    private void processBlockFile() throws IOException {
        Path blockPath = Paths.get("Program_Files/lastblock.log");
        String ledgerOutputFile = "Program_Files/lastblockledger.log";
        String finalLedgerFile = "Program_Files/ledger_final.log";
        
        try (Stream<String> lines = Files.lines(blockPath, StandardCharsets.UTF_8)) {
            lines.map(this::extractRelevantData)
                 .filter(this::isValidBlockData)
                 .forEach(line -> writeToLedgerFiles(line, ledgerOutputFile, finalLedgerFile));
        }
    }
    
    /**
     * Extract relevant data from line (skip first 49 characters)
     */
    private String extractRelevantData(String line) {
        try {
            return line.length() > 49 ? line.substring(49) : "";
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }
    
    /**
     * Check if the line contains valid block data (starts with digit)
     */
    private boolean isValidBlockData(String line) {
        return !line.isEmpty() && Character.isDigit(line.charAt(0));
    }
    
    /**
     * Write processed line to both ledger files
     */
    private void writeToLedgerFiles(String line, String ledgerFile, String finalLedgerFile) {
        try {
            // Write to block ledger
            Files.write(Paths.get(ledgerFile), line.getBytes(), 
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            // Write to final ledger with appropriate spacing
            File finalFile = new File(finalLedgerFile);
            if (finalFile.exists() && finalFile.length() > 0) {
                Files.write(Paths.get(finalLedgerFile), (" " + line.trim()).getBytes(),
                           StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(Paths.get(finalLedgerFile), line.getBytes(),
                           StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            System.err.println("Error writing to ledger files: " + e.getMessage());
        }
    }
}