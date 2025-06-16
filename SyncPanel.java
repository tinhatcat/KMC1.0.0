import java.io.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * GUI panel for synchronizing KMCoin ledger files from Downloads folder.
 * Handles file copying, conversion, and cleanup operations.
 */
public class SyncPanel extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = -6064086166669645075L;
    
    // Constants
    private static final String WINDOW_TITLE = "KMCoin Sync";
    private static final String ICON_PATH = "icon3.png";
    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 200;
    
    // File paths
    private static final String DOWNLOADS_DIR = System.getProperty("user.home") + "/Downloads";
    private static final String LEDGER_DIR = "ledger_KMC";
    private static final String PROGRAM_FILES_DIR = "Program_Files";
    
    // Download file names
    private static final String PLAYER_INFO_TXT = "player_info.txt";
    private static final String PLAYER_INFO_LOG = "player_info.log";
    private static final String LEDGER_CURRENT_TXT = "ledger_current.txt";
    private static final String LEDGER_CURRENT_LOG = "ledger_current.log";
    
    // UI Components
    private JPanel mainPanel;
    private JLabel instructionLabel1;
    private JLabel instructionLabel2;
    private JLabel instructionLabel3;
    private JButton readyButton;
    private JButton cancelButton;
    
    // Public field for external access (kept from original)
    public static int mode;
    
    /**
     * Constructs and displays the sync panel window.
     */
    public SyncPanel() {
        initializeWindow();
        createComponents();
        layoutComponents();
        finalizeWindow();
    }
    
    /**
     * Initializes the basic window properties.
     */
    private void initializeWindow() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setWindowIcon();
    }
    
    /**
     * Sets the window icon if available.
     */
    private void setWindowIcon() {
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(ICON_PATH);
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Warning: Could not load window icon from " + ICON_PATH);
        }
    }
    
    /**
     * Creates the UI components.
     */
    private void createComponents() {
        instructionLabel1 = new JLabel("  1. Download ledger and player_info from discord.");
        instructionLabel2 = new JLabel("  2. Files are moved automatically with download.");
        instructionLabel3 = new JLabel("  3. When complete, click 'Ready'.");
        
        readyButton = new JButton("Ready");
        cancelButton = new JButton("Cancel");
        
        readyButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        mainPanel = new JPanel(new GridLayout(5, 1));
    }
    
    /**
     * Arranges components in the window layout.
     */
    private void layoutComponents() {
        mainPanel.add(instructionLabel1);
        mainPanel.add(instructionLabel2);
        mainPanel.add(instructionLabel3);
        mainPanel.add(readyButton);
        mainPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Finalizes window setup and makes it visible.
     */
    private void finalizeWindow() {
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
    
    /**
     * Handles button click events.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == readyButton) {
            handleReadyButton();
        } else if (e.getSource() == cancelButton) {
            handleCancelButton();
        }
    }
    
    /**
     * Handles the Ready button click - processes and copies files.
     */
    private void handleReadyButton() {
        try {
            processDownloadedFiles();
            cleanupDownloadedFiles();
            finalizeSync();
            dispose();
        } catch (Exception e) {
            System.err.println("Error during sync process: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the Cancel button click - closes application.
     */
    private void handleCancelButton() {
        dispose();
        System.exit(0);
    }
    
    /**
     * Processes all downloaded files by copying/converting them to the ledger directory.
     */
    private void processDownloadedFiles() throws IOException {
        File playerInfoTxt = new File(DOWNLOADS_DIR, PLAYER_INFO_TXT);
        File playerInfoLog = new File(DOWNLOADS_DIR, PLAYER_INFO_LOG);
        File ledgerCurrentTxt = new File(DOWNLOADS_DIR, LEDGER_CURRENT_TXT);
        File ledgerCurrentLog = new File(DOWNLOADS_DIR, LEDGER_CURRENT_LOG);
        
        if (isValidFile(playerInfoTxt)) {
            convertTextToLog(playerInfoTxt, new File(LEDGER_DIR, PLAYER_INFO_LOG));
        }
        
        if (isValidFile(playerInfoLog)) {
            convertLogToLog(playerInfoLog, new File(LEDGER_DIR, PLAYER_INFO_LOG));
        }
        
        if (isValidFile(ledgerCurrentLog)) {
            copyFile(ledgerCurrentLog, new File(LEDGER_DIR, LEDGER_CURRENT_TXT));
        }
        
        if (isValidFile(ledgerCurrentTxt)) {
            copyFile(ledgerCurrentTxt, new File(LEDGER_DIR, LEDGER_CURRENT_TXT));
        }
    }
    
    /**
     * Cleans up downloaded files after processing.
     */
    private void cleanupDownloadedFiles() {
        String[] filesToDelete = {PLAYER_INFO_TXT, PLAYER_INFO_LOG, LEDGER_CURRENT_TXT, LEDGER_CURRENT_LOG};
        
        for (String fileName : filesToDelete) {
            File file = new File(DOWNLOADS_DIR, fileName);
            if (file.exists()) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    System.err.println("Warning: Could not delete " + fileName + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Finalizes the sync process by setting mode and creating start loop file.
     */
    private void finalizeSync() throws IOException {
        mode = 4;
        System.out.println("Sync mode set to: " + mode);
        
        // Create start loop file
        File startLoopFile = new File(PROGRAM_FILES_DIR, "startloop.log");
        startLoopFile.getParentFile().mkdirs(); // Ensure directory exists
        
        try (FileWriter writer = new FileWriter(startLoopFile)) {
            writer.write('S');
        }
    }
    
    /**
     * Checks if a file exists and is not empty.
     */
    private boolean isValidFile(File file) {
        return file.exists() && file.length() > 0;
    }
    
    /**
     * Converts a text file to log format by adding newlines.
     */
    private void convertTextToLog(File sourceFile, File destinationFile) throws IOException {
        destinationFile.getParentFile().mkdirs(); // Ensure directory exists
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }
    }
    
    /**
     * Converts a log file to another log file format.
     */
    private void convertLogToLog(File sourceFile, File destinationFile) throws IOException {
        destinationFile.getParentFile().mkdirs(); // Ensure directory exists
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }
    }
    
    /**
     * Copies a file from source to destination.
     */
    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        destinationFile.getParentFile().mkdirs(); // Ensure directory exists
        
        Path sourcePath = sourceFile.toPath();
        Path destinationPath = destinationFile.toPath();
        
        Files.copy(sourcePath, destinationPath, 
                  StandardCopyOption.REPLACE_EXISTING, 
                  StandardCopyOption.COPY_ATTRIBUTES);
    }
}