import java.io.*;

public class PlayerInfoReformatter {
    
    public PlayerInfoReformatter() throws IOException {
        reformatPlayerInfo();
    }
    
    public void reformatPlayerInfo() throws IOException {
        File inputFile = new File("ledger_KMC/player_info.log");
        File outputFile = new File("ledger_KMC/player_info_unformatted.log");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.replace("\n", " "));
            }
        }
    }
}