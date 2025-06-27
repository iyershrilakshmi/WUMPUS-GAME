import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JFrame;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class StartPanel extends JPanel {
    private Image backgroundImage;
    private float alpha = 0.8f; // Transparency level
    private int offsetX = 0;
    private int offsetY = 0;
    private final int step = 2; // Step size for moving background
    private final int boundWidth = 1800;
    private final int boundHeight = 1000;

    public StartPanel(JFrame frame) {
        try {
            backgroundImage = ImageIO.read(new File("image1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("Hunt the Wumpus", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        JButton startButton = new JButton("Click to Start");
        startButton.setFont(new Font("Serif", Font.PLAIN, 24));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                GamePanel gamePanel = new GamePanel();
                frame.add(gamePanel);
                frame.revalidate();
                frame.repaint();
                gamePanel.requestFocusInWindow();
            }
        });
        gbc.gridy = 1;
        add(startButton, gbc);

        // Timer to move background
        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                offsetX += step;
                offsetY += step;
                if (offsetX > boundWidth - getWidth()) offsetX = 0;
                if (offsetY > boundHeight - getHeight()) offsetY = 0;
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Draw zoomed-in portion of the background
            int zoomWidth = getWidth();
            int zoomHeight = getHeight();
            g2d.drawImage(backgroundImage, 0, 0, zoomWidth, zoomHeight, offsetX, offsetY, offsetX + zoomWidth, offsetY + zoomHeight, null);

            g2d.dispose();
        }
    }
}
