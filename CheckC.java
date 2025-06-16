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
                    System.out.println("[CONSENSUS] Unmatched block detected - writing to sync file");
                    unsync++;

                    // Write to Program_Files/synced.txt
                    writer2.write(line);
                    writer2.newLine();
                }
            }
            reader2.close();
            writer2.close();
        }
        catch(IOException e2){System.err.println("An error occurred: " + e2.getMessage());}

        if(syncing>=2) {
            System.out.println("[CONSENSUS] Player has matched at least 2/3 consensus! Clearing output files.");
            new PrintWriter("output.txt").close();
            new PrintWriter("discordM.txt").close();
            unsync=0;
        }

        if(unsync>=2&&syncing<=1) {
            System.out.println("[CONSENSUS] Checking if unmatched lines match each other");
            
            // Test to see if the two different lines match
            try (BufferedReader reader3 = new BufferedReader(new FileReader(destinationFilePath2))) {
                String line3;
                String previousLine = null;

                while ((line3 = reader3.readLine()) != null) {
                    int findex = line3.indexOf(" ");
                    String block = line3.substring(0, findex);
                    System.out.println("[PARSER] Block ID: " + block);
                    String noblock = line3.substring(findex+1);
                    
                    int findex2 = noblock.indexOf(" ");
                    String player = noblock.substring(0, findex2);
                    System.out.println("[PARSER] Player: " + player);
                    String noplayer = noblock.substring(findex2+1);
                    int findex3 = noplayer.indexOf(" ");
                    String hash = noplayer.substring(0, findex3);
                    System.out.println("[PARSER] Hash: " + hash);

                    // Check if this line matches the previous line
                    if(previousLine != null && line3.equals(previousLine)) {
                        System.out.println("[CONSENSUS] Other players have matched at least 2/3 consensus!");

                        // Check if ledger_current contains current McBlock sent by other 2 players
                        String fileP = "Program_Files/lastblockledger.log";
                        String ledger = Files.readString(Paths.get(fileP));

                        // Will take first 100 chars of block. Can't do all because may have missed player hash or tx
                        if(line3.contains(ledger)) {
                            System.out.println("[BLOCK] Processing malformed block: " + line3);
                            System.out.println("[BLOCK] Player received last block but it was written incorrectly - correcting...");
                            
                            // Extract BLOCK, PLAYER, TXS, HASH to correct location and let natural loop fix
                            // Copy ledger_currentCOPY to ledger_current to remove malformed block
                            // Copy player_infoCOPY to player_info to remove malformed information
                            Path source3t = Paths.get("ledger_KMC/ledger_currentCOPY.txt");
                            Path target3t = Paths.get("ledger_KMC/ledger_current.txt");

                            Path source4t = Paths.get("ledger_KMC/player_infoCOPY.log");
                            Path target4t = Paths.get("ledger_KMC/player_info.log");

                            try {
                                Files.copy(source3t, target3t, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("[FILE] Ledger backup restored successfully");
                                Files.copy(source4t, target4t, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("[FILE] Player info backup restored successfully");
                            }
                            catch (IOException e3te) {System.err.println("Error copying file: " + e3te.getMessage());}

                            // Write correct block info to normal files for KMCOIN to update
                            new PrintWriter("Program_Files/lastplayerledger.log").close();
                            String file1 = "Program_Files/lastplayerledger.log";
                            String S1 = Files.readString(Paths.get(file1));

                            try (FileWriter fileWriter1 = new FileWriter(file1, true)) {
                                fileWriter1.write(" "+player);
                                fileWriter1.close();
                            } 
                            catch (IOException e1) {e1.printStackTrace();}

                            new PrintWriter("Program_Files/lastblockledger.log").close();
                            String file2 = "Program_Files/lastblockledger.log";
                            String S2 = Files.readString(Paths.get(file2));

                            try (FileWriter fileWriter2 = new FileWriter(file2, true)) {
                                fileWriter2.write(block);
                                fileWriter2.close();
                            } 
                            catch (IOException e2) {e2.printStackTrace();}

                            new PrintWriter("Program_Files/lastblockhash.log").close();
                            String file3 = "Program_Files/lastblockhash.log";
                            String S3 = Files.readString(Paths.get(file3));

                            try (FileWriter fileWriter3 = new FileWriter(file3, true)) {
                                fileWriter3.write(" "+hash);
                                fileWriter3.close();
                            } 
                            catch (IOException e3) {e3.printStackTrace();}

                            new PrintWriter("Program_Files/lastblock.log").close();
                            String filea = "Program_Files/lastblock.log";
                            String Sa = Files.readString(Paths.get(filea));

                            try (FileWriter fileWritera = new FileWriter(filea, true)) {
                                fileWritera.write(block);
                                fileWritera.close();
                            } 
                            catch (IOException ea) {ea.printStackTrace();}

                            String filec = "Program_Files/resync.log";
                            String Sc = Files.readString(Paths.get(filec));

                            try (FileWriter fileWriterc = new FileWriter(filec, true)) {
                                fileWriterc.write("x");
                                fileWriterc.close();
                            } 
                            catch (IOException ec) {ec.printStackTrace();}

                            // Write correct TX info to latestTxs for KMCOIN to update - Updated for multiple TXs
                            if(line3.contains("&")&&line3.contains("_")&&line3.contains(",")&&line3.contains("$")) {
                                System.out.println("[TX] Processing transactions in corrected block...");

                                // Extract all transactions from the line
                                List<String> transactions = extractAllTransactions(noplayer);
                                System.out.println("[TX] Found " + transactions.size() + " transaction(s) to process");

                                // Process each transaction
                                for(String sTx : transactions) {
                                    System.out.println("[TX] Processing transaction: " + sTx);
                                    int txHamp = sTx.indexOf("&");
                                    String walletS = sTx.substring(0, txHamp);
                                    System.out.println("[TX] Wallet: " + walletS);

                                    String dPath5 = "ledger_KMC/player_info.log";
                                    String playername;
                                    String nextl;
                                    String Rplayer;
                                    String line5;

                                    try (BufferedReader reader5 = new BufferedReader(new FileReader(dPath5))) {
                                        while ((line5 = reader5.readLine()) != null) {
                                            playername = line5;
                                            nextl = reader5.readLine();

                                            if(nextl.equals("@"+walletS)) {
                                                Rplayer = playername;

                                                String dP2 = "Program_Files/latestTxs.log";
                                                try (BufferedWriter writer22 = new BufferedWriter(new FileWriter(dP2, true))) {
                                                    writer22.write("[00:00:00] [Render thread/INFO]: [CHAT] <"+Rplayer+"> "+sTx);
                                                    System.out.println("[TX] Added transaction for player: " + Rplayer);
                                                    writer22.newLine();
                                                    writer22.close();
                                                }
                                            }
                                        }
                                        reader5.close();
                                    }
                                }
                            }
                        }
                        else {
                            // Missed block... Write correct info to normal files for KMCoin to update
                            System.out.println("[BLOCK] Processing missed block - updating ledger files...");

                            new PrintWriter("Program_Files/lastplayerledger.log").close();
                            String file4 = "Program_Files/lastplayerledger.log";
                            String S4 = Files.readString(Paths.get(file4));

                            try (FileWriter fileWriter4 = new FileWriter(file4, true)) {
                                fileWriter4.write(" "+player);
                                fileWriter4.close();
                            } 
                            catch (IOException e4) {e4.printStackTrace();}

                            new PrintWriter("Program_Files/lastblockledger.log").close();
                            String file5 = "Program_Files/lastblockledger.log";
                            String S5 = Files.readString(Paths.get(file5));

                            try (FileWriter fileWriter5 = new FileWriter(file5, true)) {
                                fileWriter5.write(block);
                                fileWriter5.close();
                            } 
                            catch (IOException e5) {e5.printStackTrace();}

                            new PrintWriter("Program_Files/lastblockhash.log").close();
                            String file6 = "Program_Files/lastblockhash.log";
                            String S6 = Files.readString(Paths.get(file6));

                            try (FileWriter fileWriter6 = new FileWriter(file6, true)) {
                                fileWriter6.write(" "+hash);
                                fileWriter6.close();
                            } 
                            catch (IOException e6) {e6.printStackTrace();}

                            new PrintWriter("Program_Files/lastblock.log").close();
                            String fileb = "Program_Files/lastblock.log";
                            String Sb = Files.readString(Paths.get(fileb));

                            try (FileWriter fileWriterb = new FileWriter(fileb, true)) {
                                fileWriterb.write(block);
                                fileWriterb.close();
                            } 
                            catch (IOException eb) {eb.printStackTrace();}

                            String filed = "Program_Files/resync.log";
                            String Sd = Files.readString(Paths.get(filed));

                            try (FileWriter fileWriterd = new FileWriter(filed, true)) {
                                fileWriterd.write("x");
                                fileWriterd.close();
                            } 
                            catch (IOException ed) {ed.printStackTrace();}

                            // Write correct TX info to latestTxs for KMCOIN to update - Updated for multiple TXs
                            if(line3.contains("&")&&line3.contains("_")&&line3.contains(",")&&line3.contains("$")) {
                                System.out.println("[TX] Processing transactions in missed block...");

                                // Extract all transactions from the line
                                List<String> transactions = extractAllTransactions(noplayer);
                                System.out.println("[TX] Found " + transactions.size() + " transaction(s) to process");

                                // Process each transaction
                                for(String sTx : transactions) {
                                    System.out.println("[TX] Processing transaction: " + sTx);
                                    int txHamp = sTx.indexOf("&");
                                    String walletS = sTx.substring(0, txHamp);
                                    System.out.println("[TX] Wallet: " + walletS);

                                    String dPath5 = "ledger_KMC/player_info.log";
                                    String playername;
                                    String nextl;
                                    String Rplayer;
                                    String line5;

                                    try (BufferedReader reader5 = new BufferedReader(new FileReader(dPath5))) {
                                        while ((line5 = reader5.readLine()) != null) {
                                            playername = line5;
                                            nextl = reader5.readLine();

                                            if(nextl.equals("@"+walletS)) {
                                                Rplayer = playername;

                                                String dP2 = "Program_Files/latestTxs.log";
                                                try (BufferedWriter writer22 = new BufferedWriter(new FileWriter(dP2, true))) {
                                                    writer22.write("[00:00:00] [Render thread/INFO]: [CHAT] <"+Rplayer+"> "+sTx);
                                                    System.out.println("[TX] Added transaction for player: " + Rplayer);
                                                    writer22.newLine();
                                                    writer22.close();
                                                }
                                            }
                                        }
                                        reader5.close();
                                    }
                                }
                            }
                        }
                        
                        new PrintWriter("output.txt").close();
                        new PrintWriter("discordM.txt").close();
                        System.out.println("[CLEANUP] Output files cleared after processing consensus");

                        // Break out of the loop after processing the match
                        break;
                    }

                    // Store current line as previous for next iteration
                    previousLine = line3;
                }
                reader3.close();
            }
            catch(IOException kittykat){}
        }
    }

    // Extract all transactions from a given string
    private static List<String> extractAllTransactions(String noplayer) {
        List<String> transactions = new ArrayList<>();
        
        // Find all transaction strings that contain the required symbols
        int currentIndex = 0;
        
        while (currentIndex < noplayer.length()) {
            // Find the next space (start of potential transaction)
            int spaceIndex = noplayer.indexOf(" ", currentIndex);
            if (spaceIndex == -1) break;
            
            // Find the next semicolon (end of transaction)
            int semiIndex = noplayer.indexOf(";", spaceIndex);
            if (semiIndex == -1) break;
            
            // Extract the potential transaction string
            String potentialTx = noplayer.substring(spaceIndex + 1, semiIndex + 1);
            
            // Check if this string contains all required transaction symbols
            if (potentialTx.contains("&") && potentialTx.contains("_") && 
                potentialTx.contains(",") && potentialTx.contains("$")) {
                transactions.add(potentialTx);
                System.out.println("[TX] Found transaction: " + potentialTx);
            }
            
            // Move to next position after the semicolon
            currentIndex = semiIndex + 1;
        }
        
        return transactions;
    }
}