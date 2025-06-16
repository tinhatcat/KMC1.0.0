import java.io.*;

public class HelpFileGenerator {
    
    public HelpFileGenerator() throws IOException {
        generateHelpFile();
    }
    
    public void generateHelpFile() throws IOException {
        String fileName = "Help.txt";
        
        // Clear existing file
        new PrintWriter(fileName).close();
        
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(buildHelpContent());
        } catch (IOException e) {
            System.err.println("An error occurred while writing help file: " + e.getMessage());
            throw e;
        }
    }
    
    private String buildHelpContent() {
        StringBuilder content = new StringBuilder();
        
        content.append("If this is your first time running KMCoin please read.\n\n\n");
        
        content.append("To be minted on the ledger and player_info page a\n")
               .append("new player must run KMCoin while mining any block.\n")
               .append("This block and the private key created by the player's\n")
               .append("password may never be changed once this block is mined.\n")
               .append("To check if a player has been added to the player_info\n")
               .append("page, see 'Sync with Chain' below. Once a player is added\n")
               .append("to the this page, they never have to run KMCoin again to\n")
               .append("receive block rewards.\n\n\n");
        
        content.append("Mining Only\n\n")
               .append("Use Player Name as seen in the Minecraft server.\n")
               .append("Leave Minecraft open as full screen or windowed.\n")
               .append("This mode will not output any block information.\n")
               .append("It will write the player's hash to ledger_current\n")
               .append("and update their balance to players running KMCoin.\n\n\n");
        
        content.append("Sync with Chain\n\n")
               .append("To sync with chain do command !sync in the kmcbot-chat\n")
               .append("located in the discord channel at discord.gg/thekittymine\n")
               .append("Download the files and click 'Ready'.\n");
        
        return content.toString();
    }
}