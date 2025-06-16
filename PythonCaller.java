import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PythonCaller {
    
    // Python script file names
    private static final String CONSEND_SCRIPT = "consend.py";
    private static final String READMSG_SCRIPT = "readmsg.py";
    
    // Process execution timeout (in seconds)
    private static final int PROCESS_TIMEOUT = 30;
    
    /**
     * Constructor that automatically executes Python scripts
     */
    public PythonCaller() {
        executePythonScripts();
    }
    
    /**
     * Executes both Python scripts with proper error handling
     */
    public void executePythonScripts() {
        try {
            executeScript(CONSEND_SCRIPT, "consend process");
            executeScript(READMSG_SCRIPT, "readmsg process");
        } catch (Exception e) {
            System.err.println("Error executing Python scripts: " + e.getMessage());
        }
    }
    
    /**
     * Executes a single Python script
     * @param scriptName the name of the Python script to execute
     * @param processDescription description for logging purposes
     * @throws IOException if the process cannot be started
     * @throws InterruptedException if the process is interrupted
     */
    private void executeScript(String scriptName, String processDescription) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "python", scriptName);
        
        try {
            Process process = processBuilder.start();
            
            // Optional: Wait for process completion with timeout
            boolean finished = process.waitFor(PROCESS_TIMEOUT, TimeUnit.SECONDS);
            
            if (!finished) {
                System.err.println("Warning: " + processDescription + " did not complete within timeout");
                process.destroyForcibly(); // Clean up if needed
            }
            
        } catch (IOException e) {
            System.err.println("Failed to start " + processDescription + ": " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            System.err.println(processDescription + " was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw e;
        }
    }
    
    /**
     * Alternative execution method that throws exceptions for caller to handle
     * @throws IOException if process execution fails
     * @throws InterruptedException if process is interrupted
     */
    public void executeWithExceptions() throws IOException, InterruptedException {
        executeScript(CONSEND_SCRIPT, "consend process");
        executeScript(READMSG_SCRIPT, "readmsg process");
    }
    
    /**
     * Executes scripts asynchronously without waiting for completion
     */
    public void executeAsync() {
        try {
            startScriptAsync(CONSEND_SCRIPT, "consend process");
            startScriptAsync(READMSG_SCRIPT, "readmsg process");
        } catch (IOException e) {
            System.err.println("Error starting Python scripts asynchronously: " + e.getMessage());
        }
    }
    
    /**
     * Starts a Python script asynchronously without waiting
     * @param scriptName the name of the Python script to execute
     * @param processDescription description for logging purposes
     * @throws IOException if the process cannot be started
     */
    private void startScriptAsync(String scriptName, String processDescription) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "python", scriptName);
        
        try {
            processBuilder.start();
            System.out.println("Started " + processDescription + " asynchronously");
        } catch (IOException e) {
            System.err.println("Failed to start " + processDescription + " asynchronously: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Cross-platform method to execute Python scripts
     * @param scriptName the Python script to execute
     * @throws IOException if process execution fails
     * @throws InterruptedException if process is interrupted
     */
    public void executeCrossPlatform(String scriptName) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        
        if (os.contains("win")) {
            // Windows
            processBuilder = new ProcessBuilder("cmd.exe", "/c", "python", scriptName);
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
            // macOS or Linux
            processBuilder = new ProcessBuilder("python3", scriptName);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
        
        Process process = processBuilder.start();
        process.waitFor();
    }
    
    /**
     * Utility method to check if Python is available on the system
     * @return true if Python is available, false otherwise
     */
    public boolean isPythonAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}