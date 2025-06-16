import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;

/**
 * Processes cryptocurrency transactions and updates player balances
 */
public class Top100Processor {
    
    // Constants for parsing transaction data
    private static final String END_OF_NAME = "> ";
    private static final String DELIMITER_HASH = "&";
    private static final String DELIMITER_UNDERSCORE = "_";
    private static final String DELIMITER_COMMA = ",";
    private static final String DELIMITER_DOLLAR = "$";
    private static final String DELIMITER_TILDE = "~";
    private static final String DELIMITER_PERCENT = "%";
    private static final String DELIMITER_SEMICOLON = ";";
    private static final String WALLET_PREFIX = "@";
    private static final String NULL_TRANSACTION = "null";
    private static final int MAX_TRANSACTIONS = 100;
    
    // File paths
    private static final Path LATEST_TRANSACTIONS_PATH = Paths.get("Program_Files/latestTxs4.log");
    private static final Path LEDGER_CURRENT_PATH = Paths.get("ledger_KMC/ledger_current.txt");
    private static final Path PLAYER_INFO_PATH = Paths.get("ledger_KMC/player_info.log");
    private static final Path LOCAL_PLAYER_PATH = Paths.get("localplayer.txt");
    private static final Path WALLET_ADDRESS_PATH = Paths.get("wallet_address.log");
    private static final Path PLAYER_BALANCE_PATH = Paths.get("player_balance.log");
    private static final Path PLAYER_TXS_PATH = Paths.get("player_txs.log");
    
    private int transactionCount = 0;
    
    public Top100Processor() throws IOException {
        processTransactions();
    }
    
    /**
     * Main method to process transactions from the log file
     */
    public void processTransactions() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(LATEST_TRANSACTIONS_PATH.toFile()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(LEDGER_CURRENT_PATH.toFile(), true))) {
            
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                TransactionData transaction = parseTransactionLine(currentLine);
                
                if (transaction != null) {
                    if (shouldProcessTransaction(transaction)) {
                        processTransaction(transaction, writer, currentLine);
                    }
                }
            }
        }
        
        updatePlayerFiles();
    }
    
    /**
     * Parses a transaction line and extracts relevant data
     */
    private TransactionData parseTransactionLine(String line) {
        try {
            int endIndex = line.indexOf(END_OF_NAME);
            int hashIndex = line.indexOf(DELIMITER_HASH);
            int underscoreIndex = line.indexOf(DELIMITER_UNDERSCORE);
            int commaIndex = line.indexOf(DELIMITER_COMMA);
            int dollarIndex = line.indexOf(DELIMITER_DOLLAR);
            int tildeIndex = line.indexOf(DELIMITER_TILDE);
            int percentIndex = line.indexOf(DELIMITER_PERCENT);
            int semicolonIndex = line.indexOf(DELIMITER_SEMICOLON);
            
            if (endIndex == -1 || hashIndex == -1 || underscoreIndex == -1 || 
                commaIndex == -1 || dollarIndex == -1 || tildeIndex == -1 || 
                percentIndex == -1 || semicolonIndex == -1) {
                return null; // Invalid line format
            }
            
            String senderName = line.substring(41, endIndex);
            String amount = line.substring(hashIndex + 1, underscoreIndex);
            String receiverWallet = line.substring(underscoreIndex + 1, commaIndex);
            String gas = line.substring(commaIndex + 1, dollarIndex);
            String transactionHash = line.substring(tildeIndex + 1, percentIndex);
            
            return new TransactionData(senderName, amount, receiverWallet, gas, transactionHash);
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("Error parsing transaction line: " + line);
            return null;
        }
    }
    
    /**
     * Determines if a transaction should be processed based on hash and count
     */
    private boolean shouldProcessTransaction(TransactionData transaction) {
        if (NULL_TRANSACTION.equals(transaction.transactionHash)) {
            return transactionCount < MAX_TRANSACTIONS;
        }
        return true; // Process all non-null transactions
    }
    
    /**
     * Processes a single transaction
     */
    private void processTransaction(TransactionData transaction, BufferedWriter writer, String originalLine) 
            throws IOException {
        
        if (NULL_TRANSACTION.equals(transaction.transactionHash)) {
            transactionCount++;
        }
        
        // Write transaction to ledger
        int writeIndex = originalLine.indexOf(END_OF_NAME);
        if (writeIndex != -1) {
            String transactionEntry = originalLine.substring(writeIndex + 2, 
                originalLine.indexOf(DELIMITER_SEMICOLON) + 1);
            writer.write(transactionEntry + " ");
        }
        
        // Update sender balance
        updateSenderBalance(transaction);
        
        // Update receiver balance
        updateReceiverBalance(transaction);
    }
    
    /**
     * Updates the sender's balance by deducting amount and gas
     */
    private void updateSenderBalance(TransactionData transaction) throws IOException {
        PlayerInfo senderInfo = findPlayerByName(transaction.senderName);
        if (senderInfo != null) {
            BigInteger currentBalance = new BigInteger(senderInfo.balance);
            BigInteger amountSent = new BigInteger(transaction.amount);
            BigInteger gasBurned = new BigInteger(transaction.gas);
            BigInteger totalDeduction = amountSent.add(gasBurned);
            BigInteger newBalance = currentBalance.subtract(totalDeduction);
            
            updatePlayerBalance(transaction.senderName, senderInfo.wallet, senderInfo.balance, newBalance.toString());
        }
    }
    
    /**
     * Updates the receiver's balance by adding the received amount
     */
    private void updateReceiverBalance(TransactionData transaction) throws IOException {
        String receiverWalletWithPrefix = WALLET_PREFIX + transaction.receiverWallet;
        PlayerInfo receiverInfo = findPlayerByWallet(receiverWalletWithPrefix);
        
        if (receiverInfo != null) {
            BigInteger currentBalance = new BigInteger(receiverInfo.balance);
            BigInteger amountReceived = new BigInteger(transaction.amount);
            BigInteger newBalance = currentBalance.add(amountReceived);
            
            updatePlayerBalance(receiverInfo.name, receiverWalletWithPrefix, receiverInfo.balance, newBalance.toString());
        }
    }
    
    /**
     * Finds a player by name in the player info file
     */
    private PlayerInfo findPlayerByName(String playerName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_INFO_PATH.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(playerName)) {
                    String wallet = reader.readLine();
                    String balance = reader.readLine();
                    return new PlayerInfo(playerName, wallet, balance);
                }
            }
        }
        return null;
    }
    
    /**
     * Finds a player by wallet address in the player info file
     */
    private PlayerInfo findPlayerByWallet(String walletAddress) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_INFO_PATH.toFile()))) {
            String line;
            String previousLine = null;
            while ((line = reader.readLine()) != null) {
                if (line.equals(walletAddress)) {
                    String balance = reader.readLine();
                    return new PlayerInfo(previousLine, walletAddress, balance);
                }
                previousLine = line;
            }
        }
        return null;
    }
    
    /**
     * Updates a player's balance in the player info file
     */
    private void updatePlayerBalance(String playerName, String wallet, String oldBalance, String newBalance) 
            throws IOException {
        try {
            String fileContent = Files.readString(PLAYER_INFO_PATH);
            String oldEntry = playerName + "\n" + wallet + "\n" + oldBalance;
            String newEntry = playerName + "\n" + wallet + "\n" + newBalance;
            String updatedContent = fileContent.replace(oldEntry, newEntry);
            Files.writeString(PLAYER_INFO_PATH, updatedContent);
        } catch (IOException e) {
            System.err.println("Error updating player balance: " + e.getMessage());
        }
    }
    
    /**
     * Updates the local player's transaction panel files
     */
    public void updatePlayerFiles() throws IOException {
        try {
            String localPlayerData = Files.readString(LOCAL_PLAYER_PATH);
            String playerName = localPlayerData.substring(1); // Remove first character
            
            PlayerInfo playerInfo = findPlayerByName(playerName);
            if (playerInfo != null) {
                // Read the transactions count (assuming it's the next line after balance)
                String transactions = getPlayerTransactions(playerName);
                
                Files.writeString(WALLET_ADDRESS_PATH, playerInfo.wallet);
                Files.writeString(PLAYER_BALANCE_PATH, playerInfo.balance);
                Files.writeString(PLAYER_TXS_PATH, transactions != null ? transactions : "0");
            }
        } catch (IOException e) {
            System.err.println("Error updating player files: " + e.getMessage());
        }
    }
    
    /**
     * Gets the transaction count for a player
     */
    private String getPlayerTransactions(String playerName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_INFO_PATH.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(playerName)) {
                    reader.readLine(); // Skip wallet
                    reader.readLine(); // Skip balance
                    return reader.readLine(); // Return transactions
                }
            }
        }
        return null;
    }
    
    /**
     * Data class to hold transaction information
     */
    private static class TransactionData {
        final String senderName;
        final String amount;
        final String receiverWallet;
        final String gas;
        final String transactionHash;
        
        TransactionData(String senderName, String amount, String receiverWallet, String gas, String transactionHash) {
            this.senderName = senderName;
            this.amount = amount;
            this.receiverWallet = receiverWallet;
            this.gas = gas;
            this.transactionHash = transactionHash;
        }
    }
    
    /**
     * Data class to hold player information
     */
    private static class PlayerInfo {
        final String name;
        final String wallet;
        final String balance;
        
        PlayerInfo(String name, String wallet, String balance) {
            this.name = name;
            this.wallet = wallet;
            this.balance = balance;
        }
    }
}