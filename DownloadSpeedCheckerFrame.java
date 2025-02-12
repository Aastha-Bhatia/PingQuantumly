import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadSpeedCheckerFrame extends JFrame {
    private boolean isRunning = false;
    private final JLabel statusLabel;
    private final JLabel speedLabel;
    private final JFrame startPageFrame;
    private final ProgressPanel progressPanel;
    // Using a 5MB test file - large enough to measure but not too large
    private final String TEST_FILE_URL = "https://download.microsoft.com/download/7/0/3/703455ee-a747-4cc8-bd3e-98a615c3aedb/dotNetFx35setup.exe";
    
    public DownloadSpeedCheckerFrame(JFrame startPageFrame) {
        super("Download Speed Checker");
        this.startPageFrame = startPageFrame;
        setResizable(false);
        setSize(500, 500);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Panel for Heading and Status
        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.setBackground(Color.BLACK);

        JLabel heading = new JLabel("DOWNLOAD SPEED CHECKER", SwingConstants.CENTER);
        heading.setFont(new Font("Monospaced", Font.BOLD, 24));
        heading.setForeground(Color.WHITE);

        statusLabel = new JLabel("Click START to begin test", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.YELLOW);

        speedLabel = new JLabel("0.00 Mbps", SwingConstants.CENTER);
        speedLabel.setFont(new Font("Arial", Font.BOLD, 36));
        speedLabel.setForeground(Color.GREEN);

        textPanel.add(heading);
        textPanel.add(statusLabel);
        textPanel.add(speedLabel);

        // Progress Panel in the center
        progressPanel = new ProgressPanel();
        progressPanel.setBackground(Color.BLACK);
        progressPanel.setPreferredSize(new Dimension(400, 200));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.BLACK);

        // Start Button
        JButton startButton = new JButton("START");
        startButton.setFocusable(false);
        startButton.setBackground(Color.GREEN);
        startButton.setForeground(Color.BLACK);
        startButton.setFont(new Font("Arial", Font.BOLD, 20));

        // Back Button
        JButton backButton = new JButton("BACK");
        backButton.setFocusable(false);
        backButton.setBackground(Color.RED);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 20));

        // Start Button Action
        startButton.addActionListener(e -> {
            if (!isRunning) {
                isRunning = true;
                startButton.setEnabled(false);
                statusLabel.setText("Testing download speed...");
                statusLabel.setForeground(Color.YELLOW);
                speedLabel.setText("0.00 Mbps");
                progressPanel.reset();
                
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        measureDownloadSpeed();
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            isRunning = false;
                            startButton.setEnabled(true);
                            if (!statusLabel.getText().contains("Error")) {
                                statusLabel.setText("Test completed. Click START to test again.");
                                statusLabel.setForeground(Color.GREEN);
                            }
                        });
                    }
                });
            }
        });

        // Back Button Action
        backButton.addActionListener(e -> {
            isRunning = false;
            dispose();
            startPageFrame.setVisible(true);
        });

        buttonPanel.add(startButton);
        buttonPanel.add(backButton);

        add(textPanel, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void measureDownloadSpeed() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(TEST_FILE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned code: " + responseCode);
            }

            long fileSize = connection.getContentLengthLong();
            if (fileSize <= 0) fileSize = 5242880; // Default to 5MB if size not provided
            
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            
            try (InputStream stream = connection.getInputStream()) {
                int bytesRead;
                long startTime = System.currentTimeMillis();
                long lastUpdateTime = startTime;
                long lastBytesRead = 0;
                
                while (isRunning && (bytesRead = stream.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;
                    long currentTime = System.currentTimeMillis();
                    
                    // Update speed display every 100ms
                    if (currentTime - lastUpdateTime >= 100) {
                        double instantSpeed = ((totalBytesRead - lastBytesRead) * 8.0) / 
                                           (0.1 * 1024 * 1024); // Speed in last 100ms
                        double averageSpeed = (totalBytesRead * 8.0) / 
                                           ((currentTime - startTime) / 1000.0 * 1024 * 1024);
                        // Use a weighted average of instant and average speed
                        double displaySpeed = (instantSpeed * 0.3) + (averageSpeed * 0.7);
                        
                        final double progress = (double) totalBytesRead / fileSize;
                        
                        SwingUtilities.invokeLater(() -> {
                            DecimalFormat df = new DecimalFormat("#.##");
                            speedLabel.setText(df.format(displaySpeed) + " Mbps");
                            progressPanel.setProgress(progress);
                            progressPanel.repaint();
                        });
                        
                        lastUpdateTime = currentTime;
                        lastBytesRead = totalBytesRead;
                    }
                }
                
                // Ensure we show 100% at the end
                if (isRunning && totalBytesRead >= fileSize * 0.99) {
                    SwingUtilities.invokeLater(() -> {
                        progressPanel.setProgress(1.0);
                        progressPanel.repaint();
                    });
                }
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Error: Cannot connect to test server. Please try again.");
                statusLabel.setForeground(Color.RED);
                speedLabel.setText("0.00 Mbps");
            });
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

// ProgressPanel class remains exactly the same
class ProgressPanel extends JPanel {
    private double progress = 0.0;
    private final Color progressColor = new Color(0, 255, 0, 80);
    private final Color borderColor = new Color(0, 255, 0, 160);
    
    public ProgressPanel() {
        setOpaque(false);
    }
    
    public void setProgress(double progress) {
        this.progress = Math.min(1.0, Math.max(0.0, progress));
    }
    
    public void reset() {
        progress = 0.0;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int progressWidth = (int) (width * 0.8);
        int progressHeight = (int) (height * 0.6);
        int x = (width - progressWidth) / 2;
        int y = (height - progressHeight) / 2;
        
        // Draw border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, progressWidth, progressHeight);
        
        // Draw progress
        int filledWidth = (int) (progressWidth * progress);
        g2d.setColor(progressColor);
        g2d.fillRect(x, y, filledWidth, progressHeight);
        
        // Draw percentage text
        String percentage = String.format("%.1f%%", progress * 100);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = width / 2 - fm.stringWidth(percentage) / 2;
        int textY = y + progressHeight + 30;
        g2d.drawString(percentage, textX, textY);
    }
}