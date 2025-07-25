package org.example;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {

    private static final String ASCII_CHARS = "@#S%?*+;:,. ";



    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startAsciiWebcam);
    }

    public static void startAsciiWebcam() {
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.QVGA.getSize());
        webcam.open();

        JFrame frame = new JFrame("ASCII Webcam Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());

        JTextArea asciiArea = new JTextArea();
        asciiArea.setFont(new Font("Monospaced", Font.PLAIN, 7));
        asciiArea.setEditable(false);
        asciiArea.setBackground(Color.BLACK);
        asciiArea.setForeground(Color.WHITE);
        asciiArea.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(asciiArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            FontMetrics metrics = asciiArea.getFontMetrics(asciiArea.getFont());
            int charWidth = metrics.charWidth('A');
            int charHeight = metrics.getHeight();

            int areaWidth = asciiArea.getWidth();
            int areaHeight = asciiArea.getHeight();

            int outputWidth = areaWidth / charWidth;
            int outputHeight = areaHeight / charHeight;

            new Thread(() -> updateAsciiLoop(webcam, asciiArea, outputWidth, outputHeight)).start();
        });
    }

    public static void updateAsciiLoop(Webcam webcam, JTextArea asciiArea, int outputWidth, int outputHeight) {
        while (true) {
            if (!webcam.isOpen()) break;

            BufferedImage image = webcam.getImage();
            if (image == null) continue;

            Image scaled = image.getScaledInstance(outputWidth, outputHeight, Image.SCALE_FAST);
            BufferedImage resized = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = resized.getGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();

            StringBuilder ascii = new StringBuilder();
            for (int y = 0; y < resized.getHeight(); y++) {
                for (int x = 0; x < resized.getWidth(); x++) {
                    int pixel = resized.getRGB(x, y) & 0xFF;
                    int index = (int) ((pixel / 255.0) * (ASCII_CHARS.length() - 1));
                    ascii.append(ASCII_CHARS.charAt(index));
                }
                ascii.append('\n');
            }

            asciiArea.setText(ascii.toString());

            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
