import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.awt.geom.Ellipse2D;

// Circular button for info icon
class CircularButton extends JButton {
    public CircularButton(String label) {
        super(label);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw circular background
        g2.setColor(Color.YELLOW);
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1);
        g2.fill(circle);
        
        // Draw text
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int size = Math.max(d.width, d.height);
        return new Dimension(size, size);
    }
}

class InfoModal extends JDialog {
    public InfoModal(JFrame parent) {
        super(parent, "About PingQuantumly", true);
        setSize(500, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(255, 255, 255, 220));
        getContentPane().setBackground(Color.WHITE);

        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE); 
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 255, 255, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Close button at top right
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 20));
        closeButton.setForeground(Color.BLACK);
        closeButton.setBackground(new Color(255, 255, 255, 0));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton.setBackground(Color.RED);
        closePanel.setBackground(new Color(255, 255, 255, 0));
        closePanel.add(closeButton);

        // App icon and title group
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(255, 255, 255, 0));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("⚡", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
        iconLabel.setForeground(Color.BLACK);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("PingQuantumly", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version 1.0.0", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        versionLabel.setForeground(Color.BLACK);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description with better formatting
        String description = "<html><div style='width: 330px; text-align: justify;'>" +
        "<p style='margin: 4px 0; font-size: 10px; line-height: 1;'>" +
        "Welcome to PingQuantumly - your comprehensive internet speed testing solution. " +
        "This application provides real-time monitoring and analysis of your network performance " +
        "through an intuitive and user-friendly interface." +
        "</p>" +
        "<p style='margin: 4px 0; font-size: 10px; line-height: 1;'>" +
        "Experience precise measurements of download speeds, upload speeds, and real-time " +
        "connection monitoring, all presented through our visually appealing speed meter and " +
        "progress tracking system." +
        "</p>" +
        "<p style='margin: 4px 0; font-size: 10px; line-height: 1'><b>Key Features:</b></p>" +
        "<ul style='margin: 2px 0; padding-left: 15px; font-size: 10px; line-height: 1'>" +
        "<li>Real-time connection monitoring</li>" +
        "<li>Advanced download speed testing</li>" +
        "<li>Reliable upload speed measurement</li>" +
        "<li>Visual performance metrics</li>" +
        "<li>Detailed progress tracking</li>" +
        "</ul></div></html>";

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        descLabel.setForeground(Color.BLACK);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Connect panel with working links
        JPanel connectPanel = new JPanel();
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
        connectPanel.setBackground(new Color(255, 255, 255, 0));
        connectPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel connectTitle = new JLabel("Connect with me", SwingConstants.CENTER);
        connectTitle.setFont(new Font("Arial", Font.BOLD, 10));
        connectTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create clickable links
        String[] linkTexts = {
            "LinkedIn: aasthabhatia-er",
            "GitHub: Aastha-Bhatia",
            "Email: aasthabhatia.er@gmail.com"
        };
        String[] urls = {
            "https://www.linkedin.com/in/aasthabhatia-er/",
            "https://github.com/Aastha-Bhatia",
            "mailto:aasthabhatia.er@gmail.com"
        };

        for (int i = 0; i < linkTexts.length; i++) {
            JLabel link = new JLabel(linkTexts[i], SwingConstants.CENTER);
            link.setFont(new Font("Arial", Font.PLAIN, 10));
            link.setForeground(new Color(0, 102, 204));
            link.setCursor(new Cursor(Cursor.HAND_CURSOR));
            link.setAlignmentX(Component.CENTER_ALIGNMENT);

            final String url = urls[i];
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(InfoModal.this,
                            "Could not open link: " + url,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            connectPanel.add(link);
            connectPanel.add(Box.createVerticalStrut(5));
        }

        // Copyright notice
        JLabel copyrightLabel = new JLabel("© 2024 Aastha Bhatia. All rights reserved.", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("Arial", Font.BOLD, 12));
        copyrightLabel.setForeground(Color.RED);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Warning message about copying
        JLabel warningLabel = new JLabel("Unauthorized copying or distribution is strictly prohibited!", SwingConstants.CENTER);
        warningLabel.setFont(new Font("Arial", Font.BOLD, 10));
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add the warning label after copyright
        contentPanel.add(warningLabel);

        // Add components with minimal spacing
        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(3));
        headerPanel.add(versionLabel);

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(connectPanel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(copyrightLabel);

        // Add panels to dialog
        add(closePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Make dialog background semi-transparent
        setUndecorated(true);
        setBackground(Color.WHITE); 
    }
}
public class StartPage {
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("PingQuantumly");
        frame.setResizable(false);
        frame.setSize(500, 500);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setLayout(new BorderLayout());

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // Create top panel for info button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topPanel.setBackground(Color.BLACK);
        
        // Create circular info button
        CircularButton infoButton = new CircularButton("i");
        infoButton.setFont(new Font("Dialog", Font.BOLD, 14));
        infoButton.setPreferredSize(new Dimension(24, 24));
        infoButton.addActionListener(e -> new InfoModal(frame).setVisible(true));
        
        topPanel.add(infoButton);

        // Panel for Heading and Description
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.BLACK);

        // Add spacing at top
        textPanel.add(Box.createVerticalStrut(10));

        // App icon
        JLabel iconLabel = new JLabel("⚡", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 48));
        iconLabel.setForeground(Color.YELLOW);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(iconLabel);

        // Add spacing
        textPanel.add(Box.createVerticalStrut(20));

        // Heading
        JLabel heading = new JLabel("PingQuantumly", SwingConstants.CENTER);
        heading.setFont(new Font("Monospaced", Font.BOLD, 32));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(heading);

        // Add spacing
        textPanel.add(Box.createVerticalStrut(15));

        // Single line description
        JLabel descLabel = new JLabel("Monitor your internet speed in real-time", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(Color.YELLOW);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(descLabel);

        // Panel for Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create buttons
        String[] buttonLabels = {
            "Check Internet Speed",
            "Check Download Speed",
            "Check Upload Speed"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBackground(new Color(0, 200, 0));
            button.setForeground(Color.BLACK);
            button.setFocusable(false);
            button.setMaximumSize(new Dimension(300, 50));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Add action listeners
            if (label.contains("Internet")) {
                button.addActionListener(e -> {
                    frame.setVisible(false);
                    new InternetSpeedMeterFrame(frame);
                });
            } else if (label.contains("Download")) {
                button.addActionListener(e -> {
                    frame.setVisible(false);
                    new DownloadSpeedCheckerFrame(frame);
                });
            } else {
                button.addActionListener(e -> {
                    frame.setVisible(false);
                    new UploadSpeedCheckerFrame(frame);
                });
            }
            
            buttonPanel.add(button);
            buttonPanel.add(Box.createVerticalStrut(20));
        }

        // Add extra spacing at the bottom of button panel
        buttonPanel.add(Box.createVerticalStrut(30));

        // Footer with yellow background
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.YELLOW);
        footerPanel.setPreferredSize(new Dimension(frame.getWidth(), 14)); 

        JLabel footerLabel = new JLabel("Created by Aastha Bhatia", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.BOLD, 10));
        footerLabel.setForeground(Color.BLACK);
        footerPanel.add(footerLabel);

        frame.add(footerPanel, BorderLayout.SOUTH); 

        mainPanel.add(topPanel, BorderLayout.NORTH);  // Add top panel with info button
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartPage::createAndShowGUI);
    }
}