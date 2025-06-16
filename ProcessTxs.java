import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProcessTxs {

    // Constants
    private static final BigInteger ZERO = new BigInteger("0");
    private static final BigInteger WRAP_NUM = new BigInteger("50000000000000000");
    private static final BigInteger WRAP_ADDRESS = new BigInteger("21000001");
    private static final Path PLAYER_INFO_PATH = Paths.get("ledger_KMC/player_info.log");
    
    // Instance variables
    private String spName;

    public ProcessTxs() throws FileNotFoundException, IOException {
        copyOneAndAllTx();
        clearFile("Program_Files/latestTxs.log");

        processTransactions();

        // Continue processing remaining transactions
        File remainingTxFile = new File("Program_Files/latestTxsB.log");
        while (remainingTxFile.length() != 0) {
            clearFiles(new String[]{"Program_Files/latestTxs2.log", "Program_Files/latestTxs3.log", "Program_Files/latestTxs5.log"});
            
            copyTxAgain();
            copyOneAndAllTx();
            processTransactions();
        }
    }

    private void processTransactions() {
        try {
            removeFakeTx();
            checkForTx();

            processFileIfExists("Program_Files/latestTxs2.log", this::ensureInts);
            processFileIfExists("Program_Files/latestTxs3.log", this::orderTxByGas);
            processFileIfExists("Program_Files/latestTxs5.log", this::cL5);
            
        } catch (Exception e) {
            System.err.println("Error processing transactions: " + e.getMessage());
        }
    }

    private void processFileIfExists(String filename, FileProcessor processor) {
        try {
            File file = new File(filename);
            if (file.exists() && file.length() != 0) {
                processor.process();
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + filename + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface FileProcessor {
        void process() throws Exception;
    }

    private void clearFile(String filename) throws FileNotFoundException {
        new PrintWriter(filename).close();
    }

    private void clearFiles(String[] filenames) {
        for (String filename : filenames) {
            try {
                clearFile(filename);
            } catch (FileNotFoundException e) {
                System.err.println("Could not clear file: " + filename);
            }
        }
    }

    public void copyOneAndAllTx() throws FileNotFoundException, IOException {
        File sourceFile = new File("Program_Files/latestTxs.log");
        File singleTxFile = new File("Program_Files/latestTxsA.log");
        File multipleTxFile = new File("Program_Files/latestTxsB.log");

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
             BufferedWriter singleWriter = new BufferedWriter(new FileWriter(singleTxFile));
             BufferedWriter multipleWriter = new BufferedWriter(new FileWriter(multipleTxFile))) {
            
            String firstLine = reader.readLine();
            if (firstLine != null) {
                System.out.println("Processing first transaction");
                singleWriter.write(firstLine);
            }

            String line;
            int additionalCount = 0;
            while ((line = reader.readLine()) != null) {
                multipleWriter.write(line + System.lineSeparator());
                additionalCount++;
            }
            
            if (additionalCount > 0) {
                System.out.println("Queued " + additionalCount + " additional transactions");
            }
        }
    }

    public void copyTxAgain() throws IOException {
        copyFile("Program_Files/latestTxsB.log", "Program_Files/latestTxs.log");
    }

    public void copyManyTx() throws IOException {
        copyFile("Program_Files/latestTxs.log", "Program_Files/latestTxsA.log");
    }

    private void copyFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public void copyOneTx() throws IOException {
        File inputFile = new File("Program_Files/latestTxsA.log");
        File outputFile = new File("Program_Files/latestTxs.log");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String firstLine = reader.readLine();
            if (firstLine != null) {
                writer.write(firstLine + System.lineSeparator());
            }
        }
    }

    public void removeFakeTx() throws IOException {
        File inputFile = new File("Program_Files/latestTxsA.log");
        File outputFile = new File("Program_Files/latestTxs2.log");
        
        System.out.println("Validating transactions...");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String currentLine;
            int validCount = 0, rejectedCount = 0;
            
            while ((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                int greaterThanCount = trimmedLine.length() - trimmedLine.replace(">", "").length();

                // Check both conditions: <= 1 ">" characters AND <= 303 total characters
                if (greaterThanCount <= 1 && currentLine.length() <= 303) {
                    writer.write(currentLine + System.lineSeparator());
                    validCount++;
                    
                    String endOfName = ">";
                    int nameIndex = currentLine.indexOf(endOfName);
                    if (nameIndex > 41) {
                        spName = currentLine.substring(41, nameIndex);
                    }
                } else {
                    rejectedCount++;
                    if (greaterThanCount > 1) {
                        System.out.println("Rejected: too many '>' chars (" + greaterThanCount + ")");
                    } else if (currentLine.length() > 303) {
                        System.out.println("Rejected: length " + currentLine.length() + " > 303");
                    }
                }
            }
            
            System.out.println("Validation complete: " + validCount + " valid, " + rejectedCount + " rejected");
        }
    }

    public void checkForTx() throws FileNotFoundException, IOException {
        String endOfName = "> ";
        String delimiter = "&";
        
        // Read all wallet addresses from latestTxs4.log
        Set<String> walletsFromTxs4 = new HashSet<>();
        File txs4File = new File("Program_Files/latestTxs4.log");
        
        if (txs4File.exists()) {
            try (BufferedReader readerTxs4 = new BufferedReader(new FileReader(txs4File))) {
                String line;
                while ((line = readerTxs4.readLine()) != null) {
                    String wallet = extractWalletAddress(line, endOfName, delimiter);
                    if (wallet != null) {
                        walletsFromTxs4.add(wallet);
                    }
                }
            }
        }
        
        // Read wallet addresses from latestTxsA.log and compare
        File txsFile = new File("Program_Files/latestTxsA.log");
        boolean matchFound = false;
        
        if (txsFile.exists()) {
            try (BufferedReader readerTxs = new BufferedReader(new FileReader(txsFile))) {
                String line;
                while ((line = readerTxs.readLine()) != null) {
                    String wallet = extractWalletAddress(line, endOfName, delimiter);
                    if (wallet != null) {
                        if (walletsFromTxs4.contains(wallet)) {
                            System.out.println("Duplicate wallet detected: " + wallet + " - One tx per player per block enforced");
                            clearLogFiles();
                            matchFound = true;
                            break;
                        }
                    }
                }
            }
        }
        
        if (!matchFound && walletsFromTxs4.size() > 0) {
            System.out.println("No wallet conflicts found - proceeding");
        }
    }

    private String extractWalletAddress(String line, String startDelimiter, String endDelimiter) {
        try {
            int startIndex = line.indexOf(startDelimiter);
            int endIndex = line.indexOf(endDelimiter);
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return line.substring(startIndex + startDelimiter.length(), endIndex);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
        }
        return null;
    }

    private void clearLogFiles() {
        String[] filesToClear = {
            "Program_Files/latestTxs.log",
            "Program_Files/latestTxs2.log", 
            "Program_Files/latestTxs3.log",
            "Program_Files/latestTxs5.log"
        };
        
        clearFiles(filesToClear);
    }

    /**
     * Orders transactions by gas amount (descending) then by player name (ascending),
     * then by sender wallet (ascending), then by transaction hash (ascending)
     * to ensure deterministic ordering across all endpoints
     */
    public void orderTxByGas() throws IOException {
        File inputFile = new File("Program_Files/latestTxs3.log");
        File compareFile = new File("Program_Files/latestTxs4.log");
        File outputFile = new File("Program_Files/latestTxs5.log");
        
        // Read all transactions from both files
        List<TransactionEntry> allTransactions = new ArrayList<>();
        
        // Read from input file
        try (BufferedReader inputReader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = inputReader.readLine()) != null) {
                TransactionEntry entry = createTransactionEntry(line);
                if (entry != null) {
                    allTransactions.add(entry);
                }
            }
        }
        
        // Read from compare file
        if (compareFile.exists()) {
            try (BufferedReader compareReader = new BufferedReader(new FileReader(compareFile))) {
                String line;
                while ((line = compareReader.readLine()) != null) {
                    TransactionEntry entry = createTransactionEntry(line);
                    if (entry != null) {
                        allTransactions.add(entry);
                    }
                }
            }
        }
        
        // Sort by gas (descending) then by player name (ascending) then by wallet (ascending) then by tx hash (ascending)
        Collections.sort(allTransactions, new Comparator<TransactionEntry>() {
            @Override
            public int compare(TransactionEntry t1, TransactionEntry t2) {
                // First compare by gas amount (descending - higher gas first)
                int gasComparison = t2.gasAmount.compareTo(t1.gasAmount);
                if (gasComparison != 0) {
                    return gasComparison;
                }
                
                // If gas amounts are equal, compare by player name (ascending - alphabetical)
                int nameComparison = t1.playerName.compareTo(t2.playerName);
                if (nameComparison != 0) {
                    return nameComparison;
                }
                
                // If player names are equal, compare by sender wallet (ascending - lowest wallet first)
                int walletComparison = t1.senderWallet.compareTo(t2.senderWallet);
                if (walletComparison != 0) {
                    return walletComparison;
                }
                
                // Final tie-breaker: compare by transaction hash (ascending)
                return t1.txHash.compareTo(t2.txHash);
            }
        });
        
        // Write sorted transactions to output file
        try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile))) {
            for (TransactionEntry entry : allTransactions) {
                outputWriter.write(entry.originalLine + System.lineSeparator());
            }
        }
        
        if (allTransactions.size() > 0) {
            System.out.println("Sorted " + allTransactions.size() + " transactions by gas/priority");
        }
    }
    
    /**
     * Creates a TransactionEntry from a transaction line
     */
    private TransactionEntry createTransactionEntry(String line) {
        String gasValue = extractGasValue(line);
        String playerName = extractPlayerName(line);
        String senderWallet = extractSenderWallet(line);
        String txHash = extractTxHash(line);
        
        if (gasValue != null && playerName != null && senderWallet != null && txHash != null) {
            try {
                BigInteger gas = new BigInteger(gasValue);
                return new TransactionEntry(line, gas, playerName, senderWallet, txHash);
            } catch (NumberFormatException e) {
                System.err.println("Invalid gas value: " + gasValue);
            }
        }
        return null;
    }
    
    /**
     * Extracts player name from between < and > characters
     */
    private String extractPlayerName(String line) {
        try {
            int startIndex = line.indexOf("<");
            int endIndex = line.indexOf(">");
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return line.substring(startIndex + 1, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting player name from line: " + line);
        }
        return null;
    }

    /**
     * Extracts sender wallet from transaction line (between "> " and "&")
     */
    private String extractSenderWallet(String line) {
        try {
            int startIndex = line.indexOf("> ");
            int endIndex = line.indexOf("&");
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return line.substring(startIndex + 2, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting sender wallet from line: " + line);
        }
        return null;
    }

    /**
     * Extracts transaction hash from transaction line (between "$" and "~")
     */
    private String extractTxHash(String line) {
        try {
            int startIndex = line.indexOf("$");
            int endIndex = line.indexOf("~");
            
            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return line.substring(startIndex + 1, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting transaction hash from line: " + line);
        }
        return null;
    }

    private String extractGasValue(String line) {
        try {
            int commaIndex = line.indexOf(",");
            int dollarIndex = line.indexOf("$");
            if (commaIndex != -1 && dollarIndex != -1 && commaIndex < dollarIndex) {
                return line.substring(commaIndex + 1, dollarIndex);
            }
        } catch (Exception e) {
            System.err.println("Error extracting gas value from line: " + line);
        }
        return null;
    }

    public void cL5() throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream("Program_Files/latestTxs5.log"), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(reader);
             Writer writer = new OutputStreamWriter(new FileOutputStream("Program_Files/latestTxs4.log"), "UTF-8");
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line.replaceAll("\\ufffd", ""));
                bufferedWriter.newLine();
            }
        }
        clearFile("Program_Files/latestTxs5.log");
    }

    public void copyManyTxB() throws IOException {
        copyFile("Program_Files/latestTxsB.log", "Program_Files/latestTxsA.log");
    }

    public void removeFirstTx() throws IOException {
        File inputFile = new File("Program_Files/latestTxsA.log");
        File outputFile = new File("Program_Files/latestTxsB.log");
        
        try (Scanner fileScanner = new Scanner(inputFile);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine(); // Skip first line
            }
            
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public void copyOneTxAgain() throws IOException {
        copyOneTx(); // This method does the same thing
    }

    public void ensureInts() throws FileNotFoundException, IOException {
        File inputFile = new File("Program_Files/latestTxs2.log");
        File outputFile = new File("Program_Files/latestTxs3.log");
        
        System.out.println("Processing transaction data...");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                processTransactionLine(currentLine, writer);
            }
        }
    }

    private void processTransactionLine(String currentLine, BufferedWriter writer) throws IOException {
        // Extract transaction components
        TransactionData txData = parseTransactionLine(currentLine);

        if (txData == null) {
            System.out.println("Invalid transaction format - skipping");
            return;
        }

        // Validate transaction data
        if (!isValidTransactionData(txData)) {
            return;
        }

        // Process the transaction with player info
        processWithPlayerInfo(txData, currentLine, writer);
    }

    private TransactionData parseTransactionLine(String line) {
        try {
            int endIndex = line.indexOf("> ");
            int hampIndex = line.indexOf("&");
            int undIndex = line.indexOf("_");
            int commIndex = line.indexOf(",");
            int aposIndex = line.indexOf("$");
            int tildIndex = line.indexOf("~");
            int percIndex = line.indexOf("%");
            int semiIndex = line.indexOf(";");

            if (endIndex == -1 || hampIndex == -1 || undIndex == -1 || 
                commIndex == -1 || aposIndex == -1 || tildIndex == -1 || 
                percIndex == -1 || semiIndex == -1) {
                return null;
            }

            TransactionData txData = new TransactionData();
            txData.senderWallet = line.substring(endIndex + 2, hampIndex);
            txData.amount = line.substring(hampIndex + 1, undIndex);
            txData.receiverWallet = line.substring(undIndex + 1, commIndex);
            txData.gas = line.substring(commIndex + 1, aposIndex);
            txData.txHash = line.substring(aposIndex + 1, tildIndex);
            txData.transHash = line.substring(tildIndex + 1, percIndex);
            txData.newPublicKey = line.substring(percIndex + 1, semiIndex);

            return txData;
        } catch (Exception e) {
            System.err.println("Error parsing transaction line: " + e.getMessage());
            return null;
        }
    }

    private boolean isValidTransactionData(TransactionData txData) {
        // Check for leading zeros
        if (txData.senderWallet.startsWith("0") || txData.amount.startsWith("0") || 
            txData.receiverWallet.startsWith("0") || txData.gas.startsWith("0")) {
            System.out.println("Rejected: values cannot start with zero");
            return false;
        }

        try {
            BigInteger senderWalletBig = new BigInteger(txData.senderWallet);
            BigInteger amountBig = new BigInteger(txData.amount);
            BigInteger receiverWalletBig = new BigInteger(txData.receiverWallet);
            BigInteger gasBig = new BigInteger(txData.gas);

            if (senderWalletBig.equals(ZERO) || amountBig.equals(ZERO) || 
                receiverWalletBig.equals(ZERO) || gasBig.equals(ZERO)) {
                System.out.println("Rejected: values cannot be zero");
                return false;
            }

            // Check if all values are numeric
            Pattern pattern = Pattern.compile("[0-9]*");
            boolean isValid = pattern.matcher(txData.senderWallet).matches() &&
                             pattern.matcher(txData.amount).matches() &&
                             pattern.matcher(txData.receiverWallet).matches() &&
                             pattern.matcher(txData.gas).matches();
            
            if (!isValid) {
                System.out.println("Rejected: non-numeric values detected");
            }
            
            return isValid;

        } catch (NumberFormatException e) {
            System.out.println("Rejected: invalid number format");
            return false;
        }
    }

    private void processWithPlayerInfo(TransactionData txData, String currentLine, BufferedWriter writer) throws IOException {
        File playerInfoFile = new File("ledger_KMC/player_info.log");
        
        try (BufferedReader playerReader = new BufferedReader(new FileReader(playerInfoFile))) {
            String line;
            while ((line = playerReader.readLine()) != null) {
                if (line.equals(spName)) {
                    if (processPlayerTransaction(txData, currentLine, writer, playerReader)) {
                        break;
                    }
                }
            }
        }
    }

    private boolean processPlayerTransaction(TransactionData txData, String currentLine, 
                                           BufferedWriter writer, BufferedReader playerReader) throws IOException {
        try {
            String walletLine = playerReader.readLine();
            if (walletLine == null) return false;
            
            String playerWallet = walletLine.substring(1);
            
            if (txData.senderWallet.equals(playerWallet)) {
                String balanceLine = playerReader.readLine();
                if (balanceLine == null) return false;
                
                BigInteger balance = new BigInteger(balanceLine);
                BigInteger totalCost = new BigInteger(txData.amount).add(new BigInteger(txData.gas));
                
                if (totalCost.compareTo(balance) < 0) {
                    return processValidTransaction(txData, currentLine, writer, playerReader, balance);
                } else {
                    System.out.println("Rejected: insufficient balance");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing player transaction: " + e.getMessage());
        }
        return false;
    }

    private boolean processValidTransaction(TransactionData txData, String currentLine, 
                                          BufferedWriter writer, BufferedReader playerReader, 
                                          BigInteger balance) throws IOException {
        try {
            String txNumLine = playerReader.readLine();
            String minedBsLine = playerReader.readLine();
            String publicKeyLine = playerReader.readLine();
            
            if (txNumLine == null || minedBsLine == null || publicKeyLine == null) {
                return false;
            }
            
            int txNum = Integer.parseInt(txNumLine);
            BigInteger txNumBig = new BigInteger(txNumLine);
            
            if (validateTransactionHash(txData, txNum, publicKeyLine)) {
                writer.write(currentLine + System.lineSeparator());
                updatePlayerInfo(txData, txNum + 1, balance);
                
                // Handle wrapping transaction
                handleWrappingTransaction(txData);
                System.out.println("Transaction accepted for " + spName);
                return true;
            } else {
                System.out.println("Rejected: invalid transaction hash");
            }
        } catch (Exception e) {
            System.err.println("Error processing valid transaction: " + e.getMessage());
        }
        return false;
    }

    private boolean validateTransactionHash(TransactionData txData, int txNum, String publicKey) {
        try {
            String calculatedHash;
            
            if (txNum == 0) {
                calculatedHash = toHexString(getSHA(txData.txHash));
            } else {
                int iterations = (txNum + 1) % 1000;
                if (iterations == 0) iterations = 1001; // Special case for multiples of 1000
                
                calculatedHash = txData.txHash;
                for (int i = 0; i < iterations; i++) {
                    calculatedHash = toHexString(getSHA(calculatedHash));
                }
            }
            
            return publicKey.equals(calculatedHash);
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hash algorithm not available: " + e.getMessage());
            return false;
        }
    }

    private void updatePlayerInfo(TransactionData txData, int newTxNum, BigInteger balance) {
        try {
            String playerInfo = Files.readString(PLAYER_INFO_PATH);
            String oldInfo = spName + "\n@" + txData.senderWallet + "\n" + balance + "\n" + (newTxNum - 1);
            String newInfo = spName + "\n@" + txData.senderWallet + "\n" + balance + "\n" + newTxNum;
            
            Files.writeString(PLAYER_INFO_PATH, playerInfo.replace(oldInfo, newInfo), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error updating player info: " + e.getMessage());
        }
    }

    private void handleWrappingTransaction(TransactionData txData) {
        BigInteger receiverWallet = new BigInteger(txData.receiverWallet);
        
        if (txData.transHash.startsWith("KMC") && receiverWallet.equals(WRAP_ADDRESS)) {
            try {
                String solAddress = txData.transHash.substring(3);
                Path wrappedPath = Paths.get("ledger_KMC/wrapped.log");
                Path lastBlockPath = Paths.get("Program_Files/lastblockledger.log");
                
                String lastBlock = Files.readString(lastBlockPath);
                int dotIndex = lastBlock.indexOf(".");
                String blockNumber = lastBlock.substring(0, dotIndex);
                
                System.out.println();
                System.out.println("*WRAPPING*");
                System.out.println(solAddress + " has wrapped " + txData.amount + " coins at block " + blockNumber + " for KMC on Sol!");
                System.out.println("Check wrapped page!!!");
                System.out.println();
                
                String wrapInfo = "\n" + txData.amount + " --> " + solAddress + " at " + blockNumber;
                Files.writeString(wrappedPath, wrapInfo, StandardOpenOption.APPEND);
                
            } catch (IOException e) {
                System.err.println("Error handling wrapping transaction: " + e.getMessage());
            }
        }
    }

    // Helper class for transaction data
    private static class TransactionData {
        String senderWallet;
        String amount;
        String receiverWallet;
        String gas;
        String txHash;
        String transHash;
        String newPublicKey;
    }
    
    // Helper class for transaction ordering - UPDATED
    private static class TransactionEntry {
        final String originalLine;
        final BigInteger gasAmount;
        final String playerName;
        final String senderWallet;
        final String txHash;
        
        TransactionEntry(String originalLine, BigInteger gasAmount, String playerName, String senderWallet, String txHash) {
            this.originalLine = originalLine;
            this.gasAmount = gasAmount;
            this.playerName = playerName;
            this.senderWallet = senderWallet;
            this.txHash = txHash;
        }
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}