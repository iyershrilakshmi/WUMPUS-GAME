import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.Timer;

public class TriviaGUI extends JFrame {
    private Trivia trivia;
    private Question currentQuestion;
    private JLabel questionLabel;
    private JButton[] optionButtons;
    private Set<Question> askedQuestions;
    private int questionCounter;
    private int correctAnswerCounter;
    private GamePanel gamePanel;
    private boolean forArrows;

    public TriviaGUI(String filePath, GamePanel gamePanel) {
        this(filePath, gamePanel, false);
    }

    public TriviaGUI(String filePath, GamePanel gamePanel, boolean forArrows) {
        this.gamePanel = gamePanel;
        this.forArrows = forArrows;
        trivia = new Trivia(filePath);
        askedQuestions = new HashSet<>();
        questionCounter = 0;
        correctAnswerCounter = 0;
        currentQuestion = trivia.getRandomQuestion(askedQuestions);
        initUI();
    }

    private void initUI() {
        setTitle("Trivia Game");
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BorderLayout());
        questionPanel.setBackground(new Color(0, 0, 128)); // Dark blue background
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        questionLabel = new JLabel(currentQuestion.getQuestion(), SwingConstants.CENTER);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        add(questionPanel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(2, 2, 10, 10));
        optionsPanel.setBackground(new Color(0, 0, 64)); // Darker blue background
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = createOptionButton(currentQuestion.getOptions()[i], (char) ('A' + i));
            optionsPanel.add(optionButtons[i]);
        }

        add(optionsPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JButton createOptionButton(String text, char option) {
        JButton button = new JButton("<html><b>" + option + ": </b>" + text + "</html>");
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 128, 128)); // Teal color
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.addActionListener(new OptionButtonListener(option, currentQuestion.getCorrectOption()));
        return button;
    }

    private void loadNextQuestion() {
        currentQuestion = trivia.getRandomQuestion(askedQuestions);
        if (currentQuestion != null) {
            questionLabel.setText(currentQuestion.getQuestion());
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText("<html><b>" + (char) ('A' + i) + ": </b>" + currentQuestion.getOptions()[i] + "</html>");
                optionButtons[i].setBackground(new Color(0, 128, 128)); // Reset button color
                optionButtons[i].setEnabled(true);
                optionButtons[i].removeActionListener(optionButtons[i].getActionListeners()[0]);
                optionButtons[i].addActionListener(new OptionButtonListener((char) ('A' + i), currentQuestion.getCorrectOption()));
            }
        } else {
            questionLabel.setText("No more questions available.");
            for (JButton button : optionButtons) {
                button.setEnabled(false);
            }
        }
    }

    private class OptionButtonListener implements ActionListener {
        private char selectedOption;
        private char correctOption;

        public OptionButtonListener(char selectedOption, char correctOption) {
            this.selectedOption = selectedOption;
            this.correctOption = correctOption;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton sourceButton = (JButton) e.getSource();
            char selectedOptionChar = selectedOption;

            if (selectedOptionChar == correctOption) {
                sourceButton.setBackground(Color.GREEN);
                correctAnswerCounter++;
            } else {
                sourceButton.setBackground(Color.RED);
                // Find and mark the correct option
                for (JButton button : optionButtons) {
                    if (button.getText().charAt(7) == correctOption) {
                        button.setBackground(Color.GREEN);
                    }
                }
            }

            // Disable all buttons after selection
            for (JButton button : optionButtons) {
                button.setEnabled(false);
            }

            askedQuestions.add(currentQuestion);
            questionCounter++;

            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (questionCounter < 3) {
                        loadNextQuestion();
                    } else {
                        gamePanel.returnFromTrivia(correctAnswerCounter);
                        dispose();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TriviaGUI gui = new TriviaGUI("trivia_questions.txt", null);
            gui.setVisible(true);
        });
    }
}





