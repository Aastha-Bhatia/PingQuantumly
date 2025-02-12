import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadSpeedCheckerFrame extends JFrame {
    private boolean isRunning = false;
    private final JLabel statusLabel;
    private final JLabel speedLabel;
    private final JFrame startPageFrame;
    private final ProgressPanel progressPanel;
    private final String TEST_UPLOAD_URL = "https://httpbin.org/post"; // Test upload endpoint
    private final int TEST_FILE_SIZE = 5 * 1024 * 1024; // 5MB test file

    public UploadSpeedCheckerFrame(JFrame startPageFrame) {
        super("Upload Speed Checker");
        this.startPageFrame = startPageFrame;
        setResizable(false);
        setSize(500, 500);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Panel for Heading and Status
        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.setBackground(Color.BLACK);

        JLabel heading = new JLabel("UPLOAD SPEED CHECKER", SwingConstants.CENTER);
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
                statusLabel.setText("Testing upload speed...");
                statusLabel.setForeground(Color.YELLOW);
                speedLabel.setText("0.00 Mbps");
                progressPanel.reset();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        measureUploadSpeed();
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

    private void measureUploadSpeed() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(TEST_UPLOAD_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(TEST_FILE_SIZE));
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Generate test data
            byte[] testData = new byte[8192]; // 8KB chunks
            for (int i = 0; i < testData.length; i++) {
                testData[i] = (byte) (i % 256);
            }

            long totalBytesWritten = 0;
            long startTime = System.currentTimeMillis();
            long lastUpdateTime = startTime;
            long lastBytesWritten = 0;

            try (OutputStream stream = connection.getOutputStream()) {
                while (isRunning && totalBytesWritten < TEST_FILE_SIZE) {
                    int bytesToWrite = (int) Math.min(testData.length, TEST_FILE_SIZE - totalBytesWritten);
                    stream.write(testData, 0, bytesToWrite);
                    stream.flush();

                    totalBytesWritten += bytesToWrite;
                    long currentTime = System.currentTimeMillis();

                    // Update speed display every 100ms
                    if (currentTime - lastUpdateTime >= 100) {
                        double instantSpeed = ((totalBytesWritten - lastBytesWritten) * 8.0) /
                                (0.1 * 1024 * 1024); // Speed in last 100ms
                        double averageSpeed = (totalBytesWritten * 8.0) /
                                ((currentTime - startTime) / 1000.0 * 1024 * 1024);
                        // Use a weighted average of instant and average speed
                        double displaySpeed = (instantSpeed * 0.3) + (averageSpeed * 0.7);

                        final double progress = (double) totalBytesWritten / TEST_FILE_SIZE;

                        SwingUtilities.invokeLater(() -> {
                            DecimalFormat df = new DecimalFormat("#.##");
                            speedLabel.setText(df.format(displaySpeed) + " Mbps");
                            progressPanel.setProgress(progress);
                            progressPanel.repaint();
                        });

                        lastUpdateTime = currentTime;
                        lastBytesWritten = totalBytesWritten;
                    }
                }
            }

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned code: " + responseCode);
            }

            // Show 100% progress at completion
            if (isRunning) {
                SwingUtilities.invokeLater(() -> {
                    progressPanel.setProgress(1.0);
                    progressPanel.repaint();
                });
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

// ProgressPanel class to match the download version
class ProgressPanel2 extends JPanel {
    private double progress = 0.0;
    private final Color progressColor = new Color(0, 255, 0, 80);
    private final Color borderColor = new Color(0, 255, 0, 160);
    
    public ProgressPanel2() {
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