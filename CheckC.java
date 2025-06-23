import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.io.*;   
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CheckC {

    String sourceFilePath2 = "output.txt";
    String destinationFilePath2 = "Program_Files/synced.txt";
    String ledgercurrent = "ledger_KMC/ledger_current.txt";
    public CheckC(){
        try {
            copyFileCharacterByCharacter2(sourceFilePath2, destinationFilePath2);
        }
        catch (Exception kitty1c){}
    }

    public static void copyFileCharacterByCharacter2(String sourceFilePath2, String destinationFilePath2) throws FileNotFoundException{

        int syncing = 0;
        int unsync = 0;

        try (BufferedReader reader2 = new BufferedReader(new FileReader(sourceFilePath2));
             BufferedWriter writer2 = new BufferedWriter(new FileWriter(destinationFilePath2))) {
            
            String lBlock = "discordM.txt";
            String lBlockS = Files.readString(Paths.get(lBlock));

            String lastBlock = "Program_Files/lastblockledger.log";
            String lastBlockS = Files.readString(Paths.get(lastBlock));

            String line;

            while ((line = reader2.readLine()) != null) {
                if(line.equals(lBlockS)) {
                    System.out.println("[CONSENSUS] Block match found - syncing count: " + (syncing + 1));
                    syncing++;
                }
                else if(line.contains(".")&&line.contains("=")) {
                    //System.out.println("[CONSENSUS] Unmatched block detected - writing to sync file");
                    unsync++;

                    // Write to Program_Files/synced.txt
                    writer2.write(line);
                    writer2.newLine();
                }
            }
        }
        catch(IOException e2){System.err.println("An error occurred: " + e2.getMessage());}

        if(syncing>=2) {
            System.out.println("[CONSENSUS] Player has matched at least 2/3 consensus! Clearing output files.");
            try {
                new PrintWriter("output.txt").close();
                new PrintWriter("discordM.txt").close();
            } catch(Exception e) {}
            unsync=0;
            return; // Exit early on successful consensus
        }

        if(unsync>=2&&syncing<=1) {
            System.out.println("[CONSENSUS] Checking if unmatched lines match each other");
            
            boolean processedBlock = false;
            List<String> syncedLines = new ArrayList<>();
            
            // Read all lines first to avoid file locking issues
            try (BufferedReader reader3 = new BufferedReader(new FileReader(destinationFilePath2))) {
                String line3;
                while ((line3 = reader3.readLine()) != null) {
                    syncedLines.add(line3);
                }
            }
            catch(IOException kittykat){
                System.err.println("Error reading synced file: " + kittykat.getMessage());
                return;
            }

            // Process lines for matches
            for(int i = 0; i < syncedLines.size() && !processedBlock; i++) {
                String currentLine = syncedLines.get(i);
                
                // Look for duplicate lines (consensus among other players)
                for(int j = i + 1; j < syncedLines.size(); j++) {
                    if(currentLine.equals(syncedLines.get(j))) {
                        System.out.println("[CONSENSUS] Other players have matched at least 2/3 consensus!");
                        
                        // Parse the consensus line
                        try {
                            int findex = currentLine.indexOf(" ");
                            String block = currentLine.substring(0, findex);
                            System.out.println("[PARSER] Block ID: " + block);
                            String noblock = currentLine.substring(findex+1);
                            
                            int findex2 = noblock.indexOf(" ");
                            String player = noblock.substring(0, findex2);
                            System.out.println("[PARSER] Player: " + player);
                            String noplayer = noblock.substring(findex2+1);
                            int findex3 = noplayer.indexOf(" ");
                            String hash = "";
                            try {
                                hash = noplayer.substring(0, findex3);
                                } catch(StringIndexOutOfBoundsException e) {
                            hash = ""; // Use entire remaining string as hash if no space found
                            }
                            System.out.println("[PARSER] Hash: " + hash);

                            // Check if ledger_current contains current McBlock sent by other 2 players
                            String fileP = "Program_Files/lastblockledger.log";
                            String ledger = "";
                            try {
                                ledger = Files.readString(Paths.get(fileP));
                            } catch(IOException e) {
                                System.err.println("Error reading ledger: " + e.getMessage());
                            }

                            // Will take first 100 chars of block. Can't do all because may have missed player hash or tx
                            String consensusFirst100 = currentLine.length() > 100 ? currentLine.substring(0, 100) : currentLine;
                            //String consensusFirst100 = syncedLines.length() > 100 ? syncedLines.substring(0, 100) : syncedLines;

System.out.println("consensusFirst100= " + consensusFirst100);
System.out.println("ledger= " + ledger);

                            if(!ledger.isEmpty() && ledger.contains(block)) {
                                System.out.println("[BLOCK] Processing malformed block: " + currentLine);
                                System.out.println("[BLOCK] Player received last block but it was written incorrectly - correcting...");
                                handleMalformedBlock(block, player, hash, noplayer);
                            }
                            else {
                                System.out.println("[BLOCK] Processing missed block - updating ledger files...");
                                handleMissedBlock(block, player, hash, noplayer);
                            }
                            
                            processedBlock = true;
                            break;
                        }
                        catch(Exception parseError) {
                            System.err.println("[ERROR] Failed to parse consensus line: " + parseError.getMessage());
                        }
                    }
                }
            }
            
            if(processedBlock) {
                // Clear output files only after successful processing
                try {
                    new PrintWriter("output.txt").close();
                    new PrintWriter("discordM.txt").close();
                    System.out.println("[CLEANUP] Output files cleared after processing consensus");
                } catch(Exception e) {
                    System.err.println("Error clearing files: " + e.getMessage());
                }
            }
        }
    }

    private static void handleMalformedBlock(String block, String player, String hash, String noplayer) {
        try {
            System.out.println("[BLOCK] Player received last block but it was written incorrectly - correcting...");
            
            // Restore from backups
            Path source3t = Paths.get("ledger_KMC/ledger_currentCOPY.txt");
            Path target3t = Paths.get("ledger_KMC/ledger_current.txt");
            Path source4t = Paths.get("ledger_KMC/player_infoCOPY.log");
            Path target4t = Paths.get("ledger_KMC/player_info.log");

            Files.copy(source3t, target3t, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[FILE] Ledger backup restored successfully");
            Files.copy(source4t, target4t, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[FILE] Player info backup restored successfully");

            // Update ledger files
            updateLedgerFiles(block, player, hash);
            
            // Process transactions
            processTransactions(noplayer);
            
        } catch(Exception e) {
            System.err.println("Error handling malformed block: " + e.getMessage());
        }
    }

    private static void handleMissedBlock(String block, String player, String hash, String noplayer) {
        try {
            System.out.println("[BLOCK] Processing missed block - updating ledger files...");
            
            // Update ledger files
            updateLedgerFiles(block, player, hash);
            
            // Process transactions
            processTransactions(noplayer);
            
        } catch(Exception e) {
            System.err.println("Error handling missed block: " + e.getMessage());
        }
    }

    private static void updateLedgerFiles(String block, String player, String hash) {
        // Update lastplayerledger.log
        writeToFile("Program_Files/lastplayerledger.log", " " + player, true);
        
        // Update lastblockledger.log
        writeToFile("Program_Files/lastblockledger.log", block, true);
        
        // Update lastblockhash.log
        writeToFile("Program_Files/lastblockhash.log", " " + hash, true);
        
        // Update lastblock.log
        writeToFile("Program_Files/lastblock.log", block, true);
        
        // Update resync.log
        writeToFile("Program_Files/resync.log", "x", false);
    }

    private static void writeToFile(String filepath, String content, boolean clearFirst) {
        try {
            if(clearFirst) {
                new PrintWriter(filepath).close();
            }
            try (FileWriter fileWriter = new FileWriter(filepath, true)) {
                fileWriter.write(content);
            }
        } catch(IOException e) {
            System.err.println("Error writing to " + filepath + ": " + e.getMessage());
        }
    }

    private static void processTransactions(String noplayer) {
        if(noplayer.contains("&") && noplayer.contains("_") && 
           noplayer.contains(",") && noplayer.contains("$")) {
            
            System.out.println("[TX] Processing transactions...");
            List<String> transactions = extractAllTransactions(noplayer);
            System.out.println("[TX] Found " + transactions.size() + " transaction(s) to process");

            for(String sTx : transactions) {
                processTransaction(sTx);
            }
        }
    }

    private static void processTransaction(String sTx) {
        try {
            System.out.println("[TX] Processing transaction: " + sTx);
            int txHamp = sTx.indexOf("&");
            if(txHamp == -1) return;
            
            String walletS = sTx.substring(0, txHamp);
            System.out.println("[TX] Wallet: " + walletS);

            String dPath5 = "ledger_KMC/player_info.log";
            
            try (BufferedReader reader5 = new BufferedReader(new FileReader(dPath5))) {
                String line5;
                while ((line5 = reader5.readLine()) != null) {
                    String playername = line5;
                    String nextl = reader5.readLine();
                    if(nextl == null) break;

                    if(nextl.equals("@" + walletS)) {
                        String dP2 = "Program_Files/latestTxs.log";
                        try (BufferedWriter writer22 = new BufferedWriter(new FileWriter(dP2, true))) {
                            writer22.write("[00:00:00] [Render thread/INFO]: [CHAT] <" + playername + "> " + sTx);
                            writer22.newLine();
                            System.out.println("[TX] Added transaction for player: " + playername);
                        }
                        break;
                    }
                }
            }
        } catch(Exception e) {
            System.err.println("Error processing transaction: " + e.getMessage());
        }
    }

    private static List<String> extractAllTransactions(String noplayer) {
        List<String> transactions = new ArrayList<>();
        
        int currentIndex = 0;
        while (currentIndex < noplayer.length()) {
            int spaceIndex = noplayer.indexOf(" ", currentIndex);
            if (spaceIndex == -1) break;
            
            int semiIndex = noplayer.indexOf(";", spaceIndex);
            if (semiIndex == -1) break;
            
            String potentialTx = noplayer.substring(spaceIndex + 1, semiIndex + 1);
            
            if (potentialTx.contains("&") && potentialTx.contains("_") && 
                potentialTx.contains(",") && potentialTx.contains("$")) {
                transactions.add(potentialTx);
                System.out.println("[TX] Found transaction: " + potentialTx);
            }
            
            currentIndex = semiIndex + 1;
        }
        
        return transactions;
    }
}
