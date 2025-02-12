import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InternetSpeedMeterFrame extends JFrame {
    private boolean isRunning = false;
    private final SpeedMeterPanel speedMeter;
    private final JLabel statusLabel;
    private final JFrame startPageFrame;

    public InternetSpeedMeterFrame(JFrame startPageFrame) {
        super("Internet Speed Checker");
        this.startPageFrame = startPageFrame;
        setResizable(false);
        setSize(500, 500);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Panel for Heading and Description
        JPanel textPanel = new JPanel(new GridLayout(3, 1));  // Changed to 3,1 to match other frames
        textPanel.setBackground(Color.BLACK);

        JLabel heading = new JLabel("INTERNET SPEED CHECKER", SwingConstants.CENTER);
        heading.setFont(new Font("Monospaced", Font.BOLD, 24));
        heading.setForeground(Color.WHITE);

        // Added an empty label to maintain spacing consistency with other frames
        JLabel emptyLabel = new JLabel("Click START to begin test", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        emptyLabel.setForeground(Color.YELLOW);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));  // Updated to match other frames
        statusLabel.setForeground(Color.GREEN);  // Changed to green to match other frames

        textPanel.add(heading);
        textPanel.add(emptyLabel);
        textPanel.add(statusLabel);

        // Speed Meter Panel
        speedMeter = new SpeedMeterPanel(statusLabel);
        speedMeter.setBackground(Color.BLACK);
        speedMeter.setPreferredSize(new Dimension(400, 200));  // Updated to match other frames

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
                emptyLabel.setText("Testing connection speed...");
                emptyLabel.setForeground(Color.YELLOW);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    while (isRunning) {
                        int pingValue = getPing();
                        SwingUtilities.invokeLater(() -> {
                            speedMeter.setSpeed(pingValue);
                            if (!statusLabel.getText().contains("No Internet")) {
                                emptyLabel.setText("Test running");
                                emptyLabel.setForeground(Color.GREEN);
                            } else {
                                emptyLabel.setText("Connection error. Please try again.");
                                emptyLabel.setForeground(Color.RED);
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
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
        add(speedMeter, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static int getPing() {
        try {
            String ipAddress = "8.8.8.8";
            InetAddress inet = InetAddress.getByName(ipAddress);

            long startTime = System.nanoTime();
            boolean reachable = inet.isReachable(5000);
            long endTime = System.nanoTime();

            if (reachable) {
                return (int) ((endTime - startTime) / 1000000);
            } else {
                return 500;
            }
        } catch (IOException e) {
            return 500;
        }
    }
}

class SpeedMeterPanel extends JPanel {
    private double angle = 180;
    private int ping = 0;
    private final JLabel statusLabel;

    public SpeedMeterPanel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
        setPreferredSize(new Dimension(400, 300));
    }

    public void setSpeed(int ping) {
        this.ping = Math.max(0, ping);
        int maxPing = 300;
        this.angle = 180 - ((Math.min(ping, maxPing) * 180.0) / maxPing);
        repaint();
        if (ping >= 500) {
            statusLabel.setText("No Internet Connection");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText(ping + " ms");
            statusLabel.setForeground(Color.GREEN);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + 50;
        int radius = 100;

        // Drawing semi-circle
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, 0, 180);

        // Labels for scale
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("0", centerX - radius - 15, centerY + 5);
        g2.drawString("150", centerX - 20, centerY - radius - 5);
        g2.drawString("300+", centerX + radius - 10, centerY + 5);

        // needle
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));
        double radian = Math.toRadians(angle);
        int needleX = centerX + (int) (radius * Math.cos(radian));
        int needleY = centerY - (int) (radius * Math.sin(radian));
        g2.drawLine(centerX, centerY, needleX, needleY);
    }
}