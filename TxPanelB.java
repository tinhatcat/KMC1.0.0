import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Enhanced GUI panel for KMCoin cryptocurrency transactions.
 * Features improved styling and interactive pending transactions popup.
 */
public class TxPanelB extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = 2427596200598150517L;
    
    // Constants
    private static final int HASH_ITERATIONS = 2002;
    private static final int HASH_LENGTH = 64;
    private static final int BATCH_SIZE = 1000;
    private static final String PROGRAM_FILES_DIR = "Program_Files";
    private static final String LEDGER_DIR = "ledger_KMC";
    private static final String WRAP_WALLET_IDENTIFIER = "21000001";
    
    // Color scheme
    private static final Color BACKGROUND_COLOR = new Color(30, 35, 45);
    private static final Color PANEL_COLOR = new Color(45, 52, 65);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color FIELD_COLOR = new Color(60, 70, 85);
    private static final Color BUTTON_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_HOVER_COLOR = new Color(100, 149, 237);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    
    // File paths
    private final Path firstBlockPath = Paths.get("first_block_mined.log");
    private final Path privateKeyPath = Paths.get("privatekey.txt");
    private final Path playerBalancePath = Paths.get("player_balance.log");
    private final Path playerBlocksMinedPath = Paths.get("player_blocks_mined.log");
    private final Path playerTxSentPath = Paths.get("player_txs.log");
    private final Path playerWalletPath = Paths.get("wallet_address.log");
    private final Path latestBlockPath = Paths.get(PROGRAM_FILES_DIR, "lastblockledger.log");
    private final Path latestPlayerPath = Paths.get(PROGRAM_FILES_DIR, "lastplayerledger.log");
    private final Path latestBlockHashPath = Paths.get(PROGRAM_FILES_DIR, "lastblockhash.log");
    private final Path latestTxsPath = Paths.get(PROGRAM_FILES_DIR, "latestTxs4.log");
    private final Path consensusHashPath = Paths.get(LEDGER_DIR, "consensus_HASH.log");
    private final Path localPlayerPath = Paths.get("localplayer.txt");
    private final Path playerInfoPath = Paths.get(LEDGER_DIR, "player_info.log");
    
    // Transaction state
    private boolean isWrapTransaction = false;
    private String currentTxHash;
    private String currentTransHash;
    private String currentNewPublicKey;
    private String playerName;
    
    // GUI Components
    private JPanel mainPanel;
    private JScrollPane mainScrollPane;
    
    // Labels
    private JLabel playerInfoLabel, txPanelLabel, latestBlockInfoLabel;
    private JLabel walletLabel, balanceLabel, txSentLabel;
    private JLabel amountLabel, receivingWalletLabel, gasLabel;
    private JLabel wrapAddressLabel, wrapAmountLabel, wrapGasLabel;
    private JLabel blockLabel, playerLabel, hashLabel, pendingTxsLabel, consensusLabel;
    
    // Text fields and areas
    private JTextField amountField, receivingWalletField, gasField;
    private JTextField wrapAddressField, wrapAmountField, wrapGasField;
    private JTextField txOutputField, wrapOutputField;
    private JTextArea walletArea, balanceArea, txSentArea;
    private JTextArea blockArea, playerArea, hashArea, pendingTxsArea, consensusArea;
    
    // Buttons
    private JButton generateTxButton, generateWrapButton, refreshButton, exitButton;
    private JButton pendingTxsButton;
    
    /**
     * Constructor initializes the GUI and loads initial data
     */
    public TxPanelB() {
        initializeGUI();
        loadPlayerInfo();
        loadBlockchainInfo();
        clearTxPanelLog();
    }
    
    /**
     * Initializes the GUI components and layout with enhanced styling
     */
    private void initializeGUI() {
        setTitle("KMCoin - Cryptocurrency Wallet");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().getImage("icon3.png"));
        
        // Set look and feel
        //try {
        //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        //} catch (Exception e) {
            // Use default look and feel if system L&F fails
        //}
        
        // Set dark theme colors
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        createComponents();
        layoutComponents();
        
        setSize(600, 700);
        //setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Creates all GUI components with enhanced styling
     */
    private void createComponents() {
        // Create styled labels
        playerInfoLabel = createStyledSectionLabel("👤 Player Information");
        txPanelLabel = createStyledSectionLabel("💰 Transaction Panel");
        latestBlockInfoLabel = createStyledSectionLabel("⛓️ Latest Block Information");
        
        // Create field labels
        walletLabel = createStyledLabel("Wallet Address:");
        balanceLabel = createStyledLabel("Balance:");
        txSentLabel = createStyledLabel("Transactions Sent:");
        
        amountLabel = createStyledLabel("Amount to Send:");
        receivingWalletLabel = createStyledLabel("Receiving Wallet:");
        gasLabel = createStyledLabel("Gas (1KMC min):");
        
        wrapAddressLabel = createStyledLabel("Solana Address:");
        wrapAmountLabel = createStyledLabel("Wrap Amount:");
        wrapGasLabel = createStyledLabel("Gas:");
        
        blockLabel = createStyledLabel("Block:");
        playerLabel = createStyledLabel("Player:");
        hashLabel = createStyledLabel("Hash:");
        pendingTxsLabel = createStyledLabel("Pending Transactions:");
        consensusLabel = createStyledLabel("Ledger Consensus:");
        
        // Create text areas with styling
        walletArea = createStyledTextArea();
        balanceArea = createStyledTextArea();
        txSentArea = createStyledTextArea();
        blockArea = createStyledTextArea();
        playerArea = createStyledTextArea();
        hashArea = createStyledTextArea();
        pendingTxsArea = createStyledTextArea();
        consensusArea = createStyledTextArea();
        
        // Create text fields with styling
        amountField = createStyledTextField();
        receivingWalletField = createStyledTextField();
        gasField = createStyledTextField();
        wrapAddressField = createStyledTextField();
        wrapAmountField = createStyledTextField();
        wrapGasField = createStyledTextField();
        txOutputField = createStyledTextField();
        wrapOutputField = createStyledTextField();
        
        // Create buttons with styling
        generateTxButton = createStyledButton("🚀 Generate Transaction", SUCCESS_COLOR);
        generateWrapButton = createStyledButton("🔄 Generate Wrap Transaction", WARNING_COLOR);
        refreshButton = createStyledButton("🔄 Refresh", BUTTON_COLOR);
        exitButton = createStyledButton("❌ Exit KMCoin", new Color(231, 76, 60));
        
        // Create special button for pending transactions
        pendingTxsButton = createStyledButton("📋 View All Pending Transactions", ACCENT_COLOR);
        
        // Add action listeners
        generateTxButton.addActionListener(this);
        generateWrapButton.addActionListener(this);
        refreshButton.addActionListener(this);
        exitButton.addActionListener(this);
        pendingTxsButton.addActionListener(this);
        
        // Add click handler for pending transactions area
        addPendingTxsClickHandler();
    }
    
    /**
     * Creates a styled section label
     */
    private JLabel createStyledSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setBorder(new EmptyBorder(10, 5, 5, 5));
        return label;
    }
    
    /**
     * Creates a styled regular label
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return label;
    }
    
    /**
     * Creates a styled text area
     */
    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea(2, 20);
        area.setBackground(FIELD_COLOR);
        area.setForeground(TEXT_COLOR);
        area.setFont(new Font("Consolas", Font.PLAIN, 10));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(5, 5, 5, 5));
        return area;
    }
    
    /**
     * Creates a styled text field
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(FIELD_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("Consolas", Font.PLAIN, 11));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        field.setCaretColor(TEXT_COLOR);
        return field;
    }
    
    /**
     * Creates a styled button with hover effects
     */
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
        
        return button;
    }
    
    /**
     * Adds click handler for pending transactions area
     */
    private void addPendingTxsClickHandler() {
        pendingTxsArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    showPendingTransactionsPopup();
                }
            }
        });
        
        // Add tooltip
        pendingTxsArea.setToolTipText("Double-click to view all pending transactions in detail");
    }
    
    /**
     * Shows a popup window with detailed pending transactions
     */
    private void showPendingTransactionsPopup() {
        try {
            String txContent = "";
            if (Files.exists(latestTxsPath)) {
                txContent = Files.readString(latestTxsPath);
                txContent = txContent
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll("Render thread/INFO: CHAT <", "");
                if (txContent.length() > 9) {
                    txContent = txContent.substring(9);
                }
            }
            
            // Create popup dialog
            JDialog popup = new JDialog(this, "Pending Transactions Details", true);
            popup.setLayout(new BorderLayout());
            popup.getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Create text area for detailed view
            JTextArea detailArea = new JTextArea(15, 50);
            detailArea.setBackground(FIELD_COLOR);
            detailArea.setForeground(TEXT_COLOR);
            detailArea.setFont(new Font("Consolas", Font.PLAIN, 11));
            detailArea.setEditable(false);
            detailArea.setLineWrap(true);
            detailArea.setWrapStyleWord(true);
            detailArea.setText(txContent.isEmpty() ? "No pending transactions" : txContent);
            
            JScrollPane scrollPane = new JScrollPane(detailArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                "Transaction Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_COLOR
            ));
            
            // Create close button
            JButton closeButton = createStyledButton("Close", BUTTON_COLOR);
            closeButton.addActionListener(e -> popup.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.add(closeButton);
            
            popup.add(scrollPane, BorderLayout.CENTER);
            popup.add(buttonPanel, BorderLayout.SOUTH);
            popup.setSize(600, 400);
            popup.setLocationRelativeTo(this);
            popup.setVisible(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading pending transactions: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Arranges components in the main panel with improved layout
     */
    private void layoutComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Player info section
        addSection("Player Information", new JComponent[][] {
            {walletLabel, walletArea},
            {balanceLabel, balanceArea},
            {txSentLabel, txSentArea}
        });
        
        // Transaction panel section
        addSection("Transaction Panel", new JComponent[][] {
            {amountLabel, amountField},
            {receivingWalletLabel, receivingWalletField},
            {gasLabel, gasField},
            {new JLabel(""), generateTxButton},
            {new JLabel("Transaction Output:"), txOutputField}
        });
        
        // Wrap transaction section
        addSection("Wrap Transactions", new JComponent[][] {
            {wrapAddressLabel, wrapAddressField},
            {wrapAmountLabel, wrapAmountField},
            {wrapGasLabel, wrapGasField},
            {new JLabel(""), generateWrapButton},
            {new JLabel("Wrap Output:"), wrapOutputField}
        });
        
        // Latest block info section
        addSection("Latest Block Information", new JComponent[][] {
            {blockLabel, blockArea},
            {playerLabel, playerArea},
            {hashLabel, hashArea},
            {pendingTxsLabel, pendingTxsArea},
            {new JLabel(""), pendingTxsButton},
            {consensusLabel, consensusArea}
        });
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel);
        
        // Create scroll pane
        mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(mainScrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds a section with title and components
     */
    private void addSection(String title, JComponent[][] components) {
        JPanel sectionPanel = new JPanel(new GridBagLayout());
        sectionPanel.setBackground(PANEL_COLOR);
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                ACCENT_COLOR
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        for (int i = 0; i < components.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            gbc.fill = GridBagConstraints.NONE;
            sectionPanel.add(components[i][0], gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            sectionPanel.add(components[i][1], gbc);
        }
        
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionPanel.getPreferredSize().height));
        mainPanel.add(sectionPanel);
        mainPanel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * Handles button click events
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == refreshButton) {
                handleRefresh();
            } else if (e.getSource() == generateWrapButton) {
                handleWrapTransaction();
            } else if (e.getSource() == generateTxButton) {
                handleRegularTransaction();
            } else if (e.getSource() == exitButton) {
                handleExit();
            } else if (e.getSource() == pendingTxsButton) {
                showPendingTransactionsPopup();
            }
        } catch (IOException ex) {
            System.err.println("Error handling action: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Operation Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads player information from files
     */
    private void loadPlayerInfo() {
        try {
            initializePlayerName();
            loadPlayerInfoFromLedger();
            
            if (Files.exists(playerBalancePath)) {
                balanceArea.setText(Files.readString(playerBalancePath));
            }
            if (Files.exists(playerTxSentPath)) {
                txSentArea.setText(Files.readString(playerTxSentPath));
            }
            if (Files.exists(playerWalletPath)) {
                walletArea.setText(Files.readString(playerWalletPath));
            }
        } catch (IOException e) {
            System.err.println("Error loading player info: " + e.getMessage());
        }
    }
    
    private void initializePlayerName() throws IOException {
        if (Files.exists(localPlayerPath)) {
            String localPlayer = Files.readString(localPlayerPath);
            if (localPlayer.length() > 1) {
                playerName = localPlayer.substring(1);
            }
        }
    }
    
    private void loadBlockchainInfo() {
        try {
            if (Files.exists(latestBlockPath)) {
                blockArea.setText(Files.readString(latestBlockPath));
            }
            if (Files.exists(latestPlayerPath)) {
                playerArea.setText(Files.readString(latestPlayerPath));
            }
            if (Files.exists(latestBlockHashPath)) {
                hashArea.setText(Files.readString(latestBlockHashPath));
            }
            if (Files.exists(consensusHashPath)) {
                consensusArea.setText(Files.readString(consensusHashPath));
            }
            if (Files.exists(latestTxsPath)) {
                String txContent = Files.readString(latestTxsPath);
                String cleanedTxContent = txContent
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll("Render thread/INFO: CHAT <", "");
                if (cleanedTxContent.length() > 9) {
                    cleanedTxContent = cleanedTxContent.substring(9);
                }
                pendingTxsArea.setText(cleanedTxContent);
            }
        } catch (IOException e) {
            System.err.println("Error loading blockchain info: " + e.getMessage());
        }
    }
    
    public void refreshPanel() throws IOException {
        if (Files.exists(playerBalancePath)) {
            balanceArea.setText(Files.readString(playerBalancePath));
            balanceArea.repaint();
        }
        if (Files.exists(playerTxSentPath)) {
            txSentArea.setText(Files.readString(playerTxSentPath));
            txSentArea.repaint();
        }
        if (Files.exists(playerWalletPath)) {
            walletArea.setText(Files.readString(playerWalletPath));
            walletArea.repaint();
        }
    }
    
    private void handleRefresh() throws IOException {
        dispose();
        try (FileWriter writer = new FileWriter(Paths.get(PROGRAM_FILES_DIR, "txPanel.log").toFile())) {
            writer.write('x');
        }
    }
    
    private void handleWrapTransaction() throws IOException {
        isWrapTransaction = true;
        generateTransactionHash();
        buildTransactionString();
    }
    
    private void handleRegularTransaction() throws IOException {
        isWrapTransaction = false;
        generateTransactionHash();
        buildTransactionString();
    }
    
    private void handleExit() {
        System.exit(0);
    }
    
    private void generateTransactionHash() throws IOException {
        if (playerName == null || playerName.isEmpty()) {
            System.err.println("Player name not initialized");
            return;
        }
        
        int txCount = getPlayerTransactionCount(playerName);
        
        if (txCount == -1) {
            System.err.println("Player not found in ledger");
            return;
        }
        
        generateHashBasedOnTxCount(txCount);
    }
    
    private int getPlayerTransactionCount(String playerName) throws IOException {
        if (!Files.exists(playerInfoPath)) {
            return -1;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(playerInfoPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(playerName)) {
                    reader.readLine(); // Skip wallet address
                    reader.readLine(); // Skip balance
                    String txCountStr = reader.readLine();
                    return Integer.parseInt(txCountStr);
                }
            }
        }
        return -1;
    }
    
    private void generateHashBasedOnTxCount(int txCount) throws IOException {
        try {
            String firstBlock = Files.readString(firstBlockPath);
            String privateKey = Files.readString(privateKeyPath);
            
            int remainder = txCount % BATCH_SIZE;
            int batch = txCount / BATCH_SIZE;
            
            if (remainder != 0) {
                generateHashWithRemainder(firstBlock, privateKey, batch, remainder);
            } else if (txCount == 0) {
                generateFirstTransactionHash(firstBlock, privateKey);
            } else {
                generateBatchTransactionHash(firstBlock, privateKey, batch);
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
        }
    }
    
    private void generateHashWithRemainder(String firstBlock, String privateKey, int batch, int remainder) 
            throws NoSuchAlgorithmException {
        remainder++;
        int iterations = HASH_ITERATIONS - remainder;
        
        String combined = batch == 0 ? firstBlock + privateKey : firstBlock + privateKey + batch;
        
        String hash = combined;
        for (int i = 1; i <= iterations; i++) {
            hash = toHexString(getSHA256(hash));
        }
        currentTxHash = hash;
    }
    
    private void generateFirstTransactionHash(String firstBlock, String privateKey) throws NoSuchAlgorithmException {
        String combined = firstBlock + privateKey;
        String hash = combined;
        
        for (int i = 1; i <= 2001; i++) {
            hash = toHexString(getSHA256(hash));
        }
        currentTxHash = hash;
    }
    
    private void generateBatchTransactionHash(String firstBlock, String privateKey, int batch) 
            throws NoSuchAlgorithmException {
        int lastBatch = batch - 1;
        String transitionCombined = lastBatch == 0 ? 
            firstBlock + privateKey : 
            firstBlock + privateKey + lastBatch;
        
        String transHash = transitionCombined;
        for (int i = 1; i <= 1001; i++) {
            transHash = toHexString(getSHA256(transHash));
        }
        currentTransHash = transHash;
        
        String publicKeyCombined = firstBlock + privateKey + batch;
        String publicKeyHash = publicKeyCombined;
        for (int i = 1; i <= HASH_ITERATIONS; i++) {
            publicKeyHash = toHexString(getSHA256(publicKeyHash));
        }
        currentNewPublicKey = publicKeyHash;
        
        String txCombined = firstBlock + privateKey + batch;
        String txHash = txCombined;
        for (int i = 1; i <= 2001; i++) {
            txHash = toHexString(getSHA256(txHash));
        }
        currentTxHash = txHash;
    }
    
    private void buildTransactionString() throws IOException {
        if (!Files.exists(playerWalletPath) || Files.readString(playerWalletPath).isEmpty()) {
            System.err.println("Player wallet not found. Please sync in Discord or mine a block.");
            return;
        }
        
        String walletAddress = Files.readString(playerWalletPath);
        if (walletAddress.length() <= 1) {
            System.err.println("Invalid wallet address");
            return;
        }
        
        String cleanWalletAddress = walletAddress.substring(1);
        String transactionString;
        
        if (isWrapTransaction) {
            transactionString = buildWrapTransactionString(cleanWalletAddress);
            wrapOutputField.setText(transactionString);
        } else {
            transactionString = buildRegularTransactionString(cleanWalletAddress);
            txOutputField.setText(transactionString);
        }
        
        resetTransactionState();
    }
    
    private String buildWrapTransactionString(String walletAddress) {
        String solanaAddress = wrapAddressField.getText();
        String wrapAmount = wrapAmountField.getText();
        String wrapGas = wrapGasField.getText();
        
        return walletAddress + "&" + wrapAmount + "_" + WRAP_WALLET_IDENTIFIER + "," + 
               wrapGas + "$" + currentTxHash + "~" + "KMC" + solanaAddress + "%" + 
               currentNewPublicKey + ";";
    }
    
    private String buildRegularTransactionString(String walletAddress) throws IOException {
        String amount = amountField.getText();
        String receivingWallet = receivingWalletField.getText();
        String gas = gasField.getText();
        
        return "<" + playerName + "> " + walletAddress + "&" + amount + "_" + 
               receivingWallet + "," + gas + "$" + currentTxHash + "~" + 
               currentTransHash + "%" + currentNewPublicKey + ";";
    }
    
    private void resetTransactionState() {
        isWrapTransaction = false;
        currentTxHash = null;
        currentTransHash = null;
        currentNewPublicKey = null;
    }
    
    private void loadPlayerInfoFromLedger() throws IOException {
        if (!Files.exists(playerInfoPath) || playerName == null || playerName.isEmpty()) {
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(playerInfoPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(playerName)) {
                    String walletAddress = reader.readLine();
                    String balance = reader.readLine();
                    String txCount = reader.readLine();
                    
                    Files.writeString(playerWalletPath, walletAddress, StandardCharsets.UTF_8);
                    Files.writeString(playerBalancePath, balance, StandardCharsets.UTF_8);
                    Files.writeString(playerTxSentPath, txCount, StandardCharsets.UTF_8);
                    return;
                }
            }
        }
    }
    
    private void clearTxPanelLog() {
        try {
            Files.write(Paths.get(PROGRAM_FILES_DIR, "txPanel.log"), new byte[0]);
        } catch (IOException e) {
            System.err.println("Error clearing tx panel log: " + e.getMessage());
        }
    }
    
    private static byte[] getSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        
        while (hexString.length() < HASH_LENGTH) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}