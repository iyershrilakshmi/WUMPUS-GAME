import java.io.*;
import java.util.*;

public class Trivia {
    private List<Question> questions = new ArrayList<>();

    public Trivia(String filePath) {
        loadQuestions(filePath);
    }

    private void loadQuestions(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("?")) {
                    String question = line;
                    String[] options = new String[4];
                    for (int i = 0; i < 4; i++) {
                        options[i] = br.readLine().substring(3).trim(); // Remove 'A) ', 'B) ', etc. and trim spaces
                    }
                    String answerLine = br.readLine().trim();
                    char correctOption = answerLine.charAt(answerLine.indexOf(' ') + 1);
                    questions.add(new Question(question, options, correctOption));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Question getRandomQuestion(Set<Question> askedQuestions) {
        List<Question> remainingQuestions = new ArrayList<>(questions);
        remainingQuestions.removeAll(askedQuestions);
        if (remainingQuestions.isEmpty()) {
            return null; // All questions have been asked
        }
        Random rand = new Random();
        return remainingQuestions.get(rand.nextInt(remainingQuestions.size()));
    }

    public static void main(String[] args) {
        Trivia trivia = new Trivia("trivia_questions.txt");
        Set<Question> askedQuestions = new HashSet<>();
        Question q = trivia.getRandomQuestion(askedQuestions);
        System.out.println(q.getQuestion());
        for (String option : q.getOptions()) {
            System.out.println(option);
        }
        System.out.println("Correct answer: " + q.getCorrectOption());
    }
}


