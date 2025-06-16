import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class KMCoinApplication extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = -6064086166669645075L;
    
    // Application state
    public static int mode;
    private int playerFoundCount = 0;
    private String syncedPlayerName;
    
    // UI Components
    private JPanel mainPanel, headerPanel, inputPanel, buttonPanel;
    private JLabel titleLabel, userLabel, passwordLabel, logoLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton miningButton, syncButton, cancelButton, helpButton;
    private JCheckBox pythonCallerCheckBox;
    
    // Custom colors for the theme
    private static final Color DARK_BG = new Color(30, 30, 35);
    private static final Color CARD_BG = new Color(45, 45, 55);
    private static final Color ACCENT_PURPLE = new Color(138, 43, 226);
    private static final Color ACCENT_CYAN = new Color(64, 224, 208);
    private static final Color TEXT_COLOR = new Color(240, 240, 240);
    private static final Color BUTTON_HOVER = new Color(75, 75, 85);
    
    // File paths
    private static final String PLAYER_BALANCE_FILE = "player_balance.log";
    private static final String PLAYER_TXS_FILE = "player_txs.log";
    private static final String WALLET_ADDRESS_FILE = "wallet_address.log";
    private static final String HELP_FILE = "Help.txt";
    private static final String PRIVATE_KEY_FILE = "privatekey.txt";
    private static final String LOCAL_PLAYER_FILE = "localplayer.txt";
    private static final String PLAYER_INFO_FILE = "ledger_KMC/player_info.log";
    
    public KMCoinApplication() {
        // Initialize on EDT if not already on it
        if (SwingUtilities.isEventDispatchThread()) {
            initializeUI();
        } else {
            SwingUtilities.invokeLater(this::initializeUI);
        }
    }
    
    private void initializeUI() {
        setupIcon();
        createComponents();
        setupLayout();
        setupFrame();
    }
    
    private void setupIcon() {
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage("icon3.png");
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }
    }
    
    private void createComponents() {
        // Create main panel with custom background
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, DARK_BG,
                    0, getHeight(), new Color(40, 40, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle grid pattern
                g2d.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i < getWidth(); i += 30) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 30) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header panel with title and logo
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        // Title with glow effect
        titleLabel = new JLabel("KMCoin Wallet", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create glow effect
                g2d.setColor(new Color(138, 43, 226, 100));
                Font font = getFont().deriveFont(Font.BOLD, 28f);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                
                // Draw glow
                for (int i = 0; i < 3; i++) {
                    g2d.drawString(text, x + i, y + i);
                    g2d.drawString(text, x - i, y - i);
                }
                
                // Draw main text
                g2d.setColor(TEXT_COLOR);
                g2d.drawString(text, x, y);
            }
        };
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setPreferredSize(new Dimension(0, 60));
        
        // Create pixel art logo (using one of your images as inspiration)
        logoLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                
                // Draw a simple pixel art coin
                int size = 40;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Outer ring
                g2d.setColor(new Color(255, 215, 0)); // Gold
                g2d.fillOval(x, y, size, size);
                
                // Inner details
                g2d.setColor(new Color(218, 165, 32)); // Darker gold
                g2d.fillOval(x + 5, y + 5, size - 10, size - 10);
                
                // Center symbol (K)
                g2d.setColor(new Color(255, 215, 0));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "K";
                int textX = x + (size - fm.stringWidth(text)) / 2;
                int textY = y + (size + fm.getAscent()) / 2;
                g2d.drawString(text, textX, textY);
            }
        };
        logoLabel.setPreferredSize(new Dimension(60, 60));
        
        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Input panel with styled components - increased width
        inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25) // Increased padding
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15); // Increased spacing
        
        // Styled labels with larger font
        userLabel = createStyledLabel("Player Name:");
        passwordLabel = createStyledLabel("Password:");
        
        // Styled input fields - much wider
        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();
        
        // Styled checkbox with better visibility
        pythonCallerCheckBox = new JCheckBox("Enable Python Caller", false) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background highlight
                if (isSelected()) {
                    g2d.setColor(new Color(138, 43, 226, 40)); // Purple background when selected
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                
                super.paintComponent(g);
            }
        };
        pythonCallerCheckBox.setForeground(new Color(255, 255, 255)); // Bright white for visibility
        pythonCallerCheckBox.setOpaque(false);
        pythonCallerCheckBox.setFont(new Font("Arial", Font.BOLD, 16)); // Larger, bold font
        pythonCallerCheckBox.setToolTipText("Check to enable Python integration during sync mode");
        pythonCallerCheckBox.setFocusPainted(false);
        pythonCallerCheckBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Increased padding
        
        // Add glow effect on hover
        pythonCallerCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pythonCallerCheckBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PURPLE, 1),
                    BorderFactory.createEmptyBorder(9, 9, 9, 9)
                ));
                pythonCallerCheckBox.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                pythonCallerCheckBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                pythonCallerCheckBox.repaint();
            }
        });
        
        // Layout input components with better spacing
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 15, 12, 15); // Extra top margin for checkbox
        inputPanel.add(pythonCallerCheckBox, gbc);
        
        // Button panel
        buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Create styled buttons
        miningButton = createStyledButton("⛏️ Mining Only", ACCENT_CYAN);
        syncButton = createStyledButton("🔄 Sync with Chain", ACCENT_PURPLE);
        cancelButton = createStyledButton("❌ Cancel", new Color(220, 53, 69));
        helpButton = createStyledButton("❓ Help", new Color(40, 167, 69));
        
        // Add action listeners
        miningButton.addActionListener(this);
        syncButton.addActionListener(this);
        cancelButton.addActionListener(this);
        helpButton.addActionListener(this);
        
        buttonPanel.add(miningButton);
        buttonPanel.add(syncButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(helpButton);
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25); // Increased column count for wider field
        field.setBackground(new Color(60, 60, 70)); // Darker background for better contrast
        field.setForeground(new Color(255, 255, 255)); // Pure white text for maximum visibility
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
            BorderFactory.createEmptyBorder(12, 18, 12, 18) // More generous padding
        ));
        field.setFont(new Font("Arial", Font.BOLD, 16));
        field.setCaretColor(new Color(255, 255, 255)); // White cursor
        field.setOpaque(true);
        field.setEditable(true);
        field.setEnabled(true);
        field.setSelectionColor(ACCENT_PURPLE);
        field.setSelectedTextColor(Color.WHITE);
        field.setPreferredSize(new Dimension(320, 45)); // Much wider field
        field.setMinimumSize(new Dimension(320, 45));
        
        // Add focus listener for better visibility feedback
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_CYAN, 3), // Brighter border when focused
                    BorderFactory.createEmptyBorder(11, 17, 11, 17)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                    BorderFactory.createEmptyBorder(12, 18, 12, 18)
                ));
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(25) { // Increased column count for wider field
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Add visual feedback for typing
                if (getPassword().length > 0) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(0, 255, 0, 20)); // Subtle green overlay when typing
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        field.setBackground(new Color(60, 60, 70)); // Darker background for better contrast
        field.setForeground(new Color(255, 255, 255)); // Pure white text for maximum visibility
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
            BorderFactory.createEmptyBorder(12, 18, 12, 18) // More generous padding
        ));
        field.setFont(new Font("Arial", Font.BOLD, 16));
        field.setCaretColor(new Color(255, 255, 255)); // White cursor
        field.setOpaque(true);
        field.setEditable(true);
        field.setEnabled(true);
        field.setSelectionColor(ACCENT_PURPLE);
        field.setSelectedTextColor(Color.WHITE);
        field.setEchoChar('●'); // Set password character to bullet
        field.setPreferredSize(new Dimension(320, 45)); // Much wider field
        field.setMinimumSize(new Dimension(320, 45));
        
        // Add focus listener for better visibility feedback
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_CYAN, 3), // Brighter border when focused
                    BorderFactory.createEmptyBorder(11, 17, 11, 17)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                    BorderFactory.createEmptyBorder(12, 18, 12, 18)
                ));
            }
        });
        
        // Add document listener for visual feedback
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
        });
        
        return field;
    }
    
    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Check hover state from client property
                Boolean hovered = (Boolean) getClientProperty("hovered");
                isHovered = (hovered != null && hovered);
                
                // Background
                if (isHovered) {
                    g2d.setColor(accentColor.brighter());
                } else {
                    g2d.setColor(accentColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Border
                g2d.setColor(accentColor.brighter());
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(getText(), x, y);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // Custom border painting handled in paintComponent
            }
        };
        
        button.setPreferredSize(new Dimension(150, 45));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                btn.putClientProperty("hovered", true);
                btn.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                JButton btn = (JButton) e.getSource();
                btn.putClientProperty("hovered", false);
                btn.repaint();
            }
        });
        
        return button;
    }
    
    private void setupLayout() {
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupFrame() {
        setTitle("KMCoin Wallet - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500); // Increased window width to accommodate wider fields
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (e.getSource() == syncButton) {
            handleSyncMode(username, password);
        } else if (e.getSource() == miningButton) {
            handleMiningMode(username, password);
        } else if (e.getSource() == cancelButton) {
            handleCancel();
        } else if (e.getSource() == helpButton) {
            handleHelp();
        }
    }
    
    private void handleSyncMode(String username, String password) {
        mode = 3;
        
        try {
            new SyncPanel();
            writeModeFile('S');
            
            // Only call PythonCaller if checkbox is selected
            if (pythonCallerCheckBox.isSelected()) {
                new PythonCaller();
                System.out.println("Python Caller enabled");
            } else {
                System.out.println("Python Caller disabled");
            }
            
            createRequiredFiles();
            saveCredentials(username, password);
            new PrintHash();
            
            clearSyncFiles();
            writeStartLoopFile();
            
            syncedPlayerName = readPlayerName();
            syncPlayerInfo();
            writeTxPanelFile();
            
            dispose();
            
        } catch (IOException e) {
            System.err.println("Error in sync mode: " + e.getMessage());
        }
    }
    
    private void handleMiningMode(String username, String password) {
        mode = 6;
        
        try {
            new MiningPanel();
            writeMiningStartFile();
            saveCredentials(username, password);
            new PrintHash();
            dispose();
            
        } catch (IOException e) {
            System.err.println("Error in mining mode: " + e.getMessage());
        }
    }
    
    private void handleCancel() {
        System.exit(0);
    }
    
    private void handleHelp() {
        File helpFile = new File(HELP_FILE);
        if (helpFile.exists()) {
            try {
                Desktop.getDesktop().open(helpFile);
            } catch (IOException e) {
                System.err.println("Could not open help file: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Help file not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveCredentials(String username, String password) throws IOException {
        Path privateKeyPath = Paths.get(PRIVATE_KEY_FILE);
        Path localPlayerPath = Paths.get(LOCAL_PLAYER_FILE);
        
        Files.writeString(privateKeyPath, password, StandardCharsets.UTF_8);
        Files.writeString(localPlayerPath, " " + username, StandardCharsets.UTF_8);
    }
    
    private String readPlayerName() throws IOException {
        Path playerPath = Paths.get(LOCAL_PLAYER_FILE);
        String fullName = Files.readString(playerPath);
        return fullName.substring(1); // Remove leading space
    }
    
    private void writeModeFile(char mode) throws IOException {
        try (FileWriter writer = new FileWriter("Program_Files/mode.log")) {
            writer.write(mode);
        }
    }
    
    private void writeStartLoopFile() throws IOException {
        try (FileWriter writer = new FileWriter("Program_Files/startloop2.log")) {
            writer.write('S');
        }
    }
    
    private void writeMiningStartFile() throws IOException {
        try (FileWriter writer = new FileWriter("Program_Files/startloop.log")) {
            writer.write('S');
        }
    }
    
    private void writeTxPanelFile() throws IOException {
        try (FileWriter writer = new FileWriter("Program_Files/txPanel.log")) {
            writer.write('x');
        }
    }
    
    private void clearSyncFiles() throws IOException {
        String[] filesToClear = {
            "Program_Files/lastplayerledger.log",
            "Program_Files/lastblock.log",
            "Program_Files/otherplayerhash.log",
            "Program_Files/latestplayers.log",
            "Program_Files/lastplayer.log"
        };
        
        for (String fileName : filesToClear) {
            new PrintWriter(fileName).close();
        }
    }
    
    private void createRequiredFiles() throws IOException {
        String[] requiredFiles = {
            "privatekey.txt", "latestcopy.log", "localplayer.txt",
            "Program_Files/latestchat.log", "Program_Files/latestblocks.log",
            "Program_Files/latestplayers.log", "Program_Files/lastblock.log",
            "Program_Files/lastblockledger.log", "Program_Files/ledger_final.log",
            "Program_Files/lastplayer.log", "Program_Files/lastplayerledger.log",
            "Program_Files/lastblockhash.log", "Program_Files/otherplayerhash.log",
            "Program_Files/otherplayerhash2.log", "Program_Files/otherplayerhash3.log",
            "Program_Files/latestcopy2.log", "Program_Files/latestcopy3.log",
            "Program_Files/lastledgerhash.log", "Program_Files/ledger_formatted.log",
            "Program_Files/hash.log", "Program_Files/hash2.log", WALLET_ADDRESS_FILE,
            "Program_Files/mode.log", "ledger_KMC/ledgerhashes.log",
            "Program_Files/ledgertemp.log", "Program_Files/ledgertempformatted.log",
            "ledger_KMC/player_info_temp.log", "Program_Files/ledgertempformattedNotx.log",
            "Program_Files/ledgertempnotx.log", "Program_Files/ledgertempformatted.log",
            PLAYER_BALANCE_FILE, PLAYER_TXS_FILE, "player_blocks_mined.log",
            "Program_Files/ledgertx.log", "Program_Files/latestTxs.log",
            "Program_Files/latestTxs2.log", "publickey.log", "first_block_mined.log",
            "Program_Files/latestTxs3.log", "Program_Files/latestTxs4.log",
            "Program_Files/latestTxs5.log", "ledger_KMC/ledger_current_HASH.log",
            "ledger_KMC/player_info_HASH.log", "ledger_KMC/player_info_unformatted.log",
            "ledger_KMC/ledgerhashes_HASH.log", "ledger_KMC/consensus_HASH.log",
            "Program_Files/txPanel.log", "ledger_KMC/wrapped.log", "discordC.txt",
            "Program_Files/readySend.txt", "discordM.txt", "Program_Files/synced.txt",
            "ledger_KMC/ledger_currentCOPY.txt", "ledger_KMC/player_infoCOPY.log",
            "Program_Files/latestTxs4COPY.log", "Program_Files/resync.log",
            "timestamp1.txt", "Program_Files/latestTxsC.log", "Program_Files/latestTxsM.log",
            "output.txt","outputTx.txt"
        };
        
        for (String fileName : requiredFiles) {
            new File(fileName).createNewFile();
        }
    }
    
    private void syncPlayerInfo() throws IOException {
        playerFoundCount = 0;
        File playerInfoFile = new File(PLAYER_INFO_FILE);
        
        if (!playerInfoFile.exists()) {
            System.err.println("Player info file not found: " + PLAYER_INFO_FILE);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(playerInfoFile))) {
            String currentLine;
            
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.equals(syncedPlayerName)) {
                    playerFoundCount++;
                    
                    String walletAddress = reader.readLine();
                    String balance = reader.readLine();
                    String transactions = reader.readLine();
                    
                    if (walletAddress != null && balance != null && transactions != null) {
                        updatePlayerFiles(walletAddress, balance, transactions);
                    }
                    break;
                }
            }
        }
        
        if (playerFoundCount == 0) {
            System.out.println("Player must mine a block to obtain a wallet address and be placed on player_info");
        }
    }
    
    private void updatePlayerFiles(String walletAddress, String balance, String transactions) throws IOException {
        Files.writeString(Paths.get(WALLET_ADDRESS_FILE), walletAddress, StandardCharsets.UTF_8);
        Files.writeString(Paths.get(PLAYER_BALANCE_FILE), balance, StandardCharsets.UTF_8);
        Files.writeString(Paths.get(PLAYER_TXS_FILE), transactions, StandardCharsets.UTF_8);
    }
}