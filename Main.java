import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Hunt the Wumpus");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        StartPanel startPanel = new StartPanel(frame);
        frame.add(startPanel);
        frame.setVisible(true);

        startPanel.requestFocusInWindow();
    }
}   


 












