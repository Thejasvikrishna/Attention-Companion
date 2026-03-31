package com.attentioncompanion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AttentionCompanionUI extends JFrame implements AttentionCompanionApp.UIUpdater {

    private AttentionCompanionApp core;
    private JButton startBtn, stopBtn, sendNowBtn;
    private JLabel statusLabel, attentionLabel, avgLabel, timeLabel;
    private JTextArea sessionLogArea;
    private JTextField emailField;
    private JProgressBar attentionBar;

    private long redStartTime = 0;
    private boolean alertSent = false;

    // Store report file paths for sending
    private String lastCsvFile = "";
    private String lastPdfFile = "";

    public AttentionCompanionUI() {
        setTitle("Attention Companion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setResizable(false);

        core = new AttentionCompanionApp(this, mat -> {});
        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Fullscreen by default
    }

    private JPanel createTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setBorder(new EmptyBorder(10, 10, 10, 10));

        startBtn = new JButton("▶ START");
        stopBtn = new JButton("⏹ STOP");

        stopBtn.setEnabled(false);

        JButton fullscreenBtn = new JButton("⛶ Fullscreen");
        fullscreenBtn.addActionListener(e -> toggleFullscreen());

        startBtn.addActionListener(e -> startSession());
        stopBtn.addActionListener(e -> stopSession());

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14));

        top.add(startBtn);
        top.add(stopBtn);
        top.add(fullscreenBtn);
        top.add(Box.createHorizontalStrut(20));
        top.add(statusLabel);
        return top;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(10, 10));

        sessionLogArea = new JTextArea();
        sessionLogArea.setEditable(false);
        sessionLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        sessionLogArea.setBackground(Color.WHITE);
        sessionLogArea.setForeground(Color.BLACK);
        sessionLogArea.setLineWrap(true);
        sessionLogArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(sessionLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Session Log"));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setPreferredSize(new Dimension(640, 480));
        logPanel.add(logScroll, BorderLayout.CENTER);

        center.add(logPanel, BorderLayout.CENTER);

        // RIGHT: Attention Metrics
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel metricsTitle = new JLabel("Attention Metrics");
        metricsTitle.setFont(metricsTitle.getFont().deriveFont(Font.BOLD, 14f));
        metricsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        attentionBar = new JProgressBar(0, 100);
        attentionBar.setStringPainted(true);
        attentionBar.setPreferredSize(new Dimension(250, 50));
        attentionBar.setMaximumSize(new Dimension(250, 50));
        attentionBar.setValue(0);
        attentionBar.setForeground(Color.GREEN);

        avgLabel = new JLabel("Avg: 0.0%");
        avgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avgLabel.setFont(new Font("Arial", Font.BOLD, 14));

        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        attentionLabel = new JLabel("0%");
        attentionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        attentionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        attentionLabel.setForeground(Color.GREEN);

        right.add(metricsTitle);
        right.add(Box.createVerticalStrut(10));
        right.add(attentionBar);
        right.add(Box.createVerticalStrut(10));
        right.add(attentionLabel);
        right.add(Box.createVerticalStrut(20));
        right.add(avgLabel);
        right.add(Box.createVerticalStrut(10));
        right.add(timeLabel);
        right.add(Box.createVerticalGlue());

        center.add(right, BorderLayout.EAST);

        return center;
    }

    private JPanel createBottomPanel() {
        JPanel emailPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        emailPanel.setBorder(BorderFactory.createTitledBorder("Alert Settings"));

        emailPanel.add(new JLabel("Send Alert To:"));
        emailField = new JTextField("student@example.com");
        emailPanel.add(emailField);



        return emailPanel;
    }

    private void toggleFullscreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            setSize(980, 750);
            setLocationRelativeTo(null);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void startSession() {
        sessionLogArea.setText("");
        lastCsvFile = "";
        lastPdfFile = "";
        redStartTime = 0;
        alertSent = false;

        log("=== SESSION STARTED ===");
        log("Face detection: ACTIVE");
        log("App monitoring: ACTIVE\n");

        core.startSession();
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        sendNowBtn.setEnabled(false);  // Disable until session stops
    }

    private void stopSession() {
        log("\n=== SESSION STOPPED ===");
        core.stopSession();
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        sendNowBtn.setEnabled(true);  // Enable AFTER session stops
    }



    @Override
    public void updateUI(double attention, double appScore, String status, int timeSec, double avgAttention) {
        SwingUtilities.invokeLater(() -> {
            int attPercent = (int)(attention * 100);

            boolean isRed = attPercent <= 50;
            attentionLabel.setText(attPercent + "%");
            attentionLabel.setForeground(isRed ? Color.RED : Color.GREEN);

            attentionBar.setValue(attPercent);
            attentionBar.setForeground(isRed ? Color.RED : Color.GREEN);
            attentionBar.setString(attPercent + "%");

            // AUTO ALERT (always enabled)
            if (isRed) {
                if (redStartTime == 0) {
                    redStartTime = System.currentTimeMillis();
                } else {
                    long redMs = System.currentTimeMillis() - redStartTime;
                    if (redMs >= 120000 && !alertSent) {
                        triggerAttentionAlert(attPercent);
                        alertSent = true;
                    }
                }
            } else {
                redStartTime = 0;
                alertSent = false;
            }

            statusLabel.setText("Status: " + status);
            avgLabel.setText(String.format("Avg: %.1f%%", avgAttention * 100));
            timeLabel.setText(String.format("Time: %02d:%02d", timeSec / 60, timeSec % 60));
        });
    }

    private void triggerAttentionAlert(int attPercent) {
         String to = emailField.getText().trim();
        if (to.isEmpty()) {
           log("⚠️ Alert not sent: recipient email empty");

            return;
        }

        log("⚠️ Attention low for 2+ minutes. Sending alert...");

        new Thread(() -> {
            String subject = "🚨 Attention Alert: Low focus detected";
            String body = "Your attention has been below 50% for more than 2 minutes.\n"
                    + "Current attention: " + attPercent + "%\n"
                    + "Time: " + LocalTime.now() + "\n\n"
                    + "Please take a short break and refocus.";

            boolean ok = EmailService.sendAlertEmail(to, subject, body);
            SwingUtilities.invokeLater(() ->
                    log(ok ? "📧 Alert email sent" : "❌ Alert email failed")
            );
        }).start();
    }

    @Override
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            sessionLogArea.append("[" + timestamp + "] " + message + "\n");
            sessionLogArea.setCaretPosition(sessionLogArea.getDocument().getLength());
        });
    }

    // Called from AttentionCompanionApp with report files
    public void setReportFiles(String csv, String pdf) {
        this.lastCsvFile = csv;
        this.lastPdfFile = pdf;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new AttentionCompanionUI().setVisible(true);
        });
    }
}
