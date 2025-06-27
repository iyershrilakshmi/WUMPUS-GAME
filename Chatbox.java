import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Chatbox extends JPanel {
    private JTextArea textArea;
    private Timer typingTimer;
    private String fullMessage;
    private int currentCharIndex;

    public Chatbox() {
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        add(textArea, BorderLayout.CENTER);
        setBackground(new Color(0, 0, 0, 150));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
    }

    public void showMessage(String message) {
        if (typingTimer != null) {
            typingTimer.cancel();
        }
        fullMessage = message;
        currentCharIndex = 0;
        textArea.setText("");
        typingTimer = new Timer();
        typingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentCharIndex < fullMessage.length()) {
                    textArea.append(String.valueOf(fullMessage.charAt(currentCharIndex)));
                    currentCharIndex++;
                } else {
                    typingTimer.cancel();
                }
            }
        }, 0, 50); // Adjust typing speed by changing the delay (50ms in this case)
    }
}
