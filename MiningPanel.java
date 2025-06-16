import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A simple GUI window that displays mining status information.
 * This window serves as a visual indicator that the mining process is running.
 */
public class MiningPanel extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = -6064086166669645075L;
    
    // Constants for window configuration
    private static final String WINDOW_TITLE = "Kitty Mining...";
    private static final String ICON_PATH = "icon3.png";
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 100;
    
    // UI Components
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JLabel encouragementLabel;
    
    // Public field for external access (kept from original)
    public static int mode;
    
    /**
     * Constructs and displays the mining panel window.
     */
    public MiningPanel() {
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
     * Sets the window icon if the icon file exists.
     */
    private void setWindowIcon() {
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(ICON_PATH);
            setIconImage(icon);
        } catch (Exception e) {
            // Icon file not found or invalid - continue without icon
            System.err.println("Warning: Could not load window icon from " + ICON_PATH);
        }
    }
    
    /**
     * Creates the UI components.
     */
    private void createComponents() {
        statusLabel = new JLabel("  Close this window to stop program.", JLabel.CENTER);
        encouragementLabel = new JLabel("     Good luck!", JLabel.CENTER);
        mainPanel = new JPanel(new GridLayout(2, 1));
    }
    
    /**
     * Arranges the components in the window layout.
     */
    private void layoutComponents() {
        mainPanel.add(statusLabel);
        mainPanel.add(encouragementLabel);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Finalizes the window setup and makes it visible.
     */
    private void finalizeWindow() {
        setLocationRelativeTo(null); // Center the window
        setResizable(false); // Prevent resizing for consistent appearance
        setVisible(true);
    }
    
    /**
     * Handles action events (currently no actions are implemented).
     * 
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // No actions currently implemented
        // This method is kept for potential future use
    }
    
    /**
     * Updates the status message displayed in the window.
     * 
     * @param message the new status message to display
     */
    public void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            repaint();
        }
    }
    
    /**
     * Updates the encouragement message displayed in the window.
     * 
     * @param message the new encouragement message to display
     */
    public void updateEncouragement(String message) {
        if (encouragementLabel != null) {
            encouragementLabel.setText(message);
            repaint();
        }
    }
}