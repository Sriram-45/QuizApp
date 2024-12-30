import java.util.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Main class
public class QuizApp {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/quiz_app";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize services
        AuthService authService = new AuthService();
        QuizService quizService = new QuizService();
        ProgressService progressService = new ProgressService();

        // Example functionality
        System.out.println("Welcome to the Quiz App!");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("Please select an option: ");
        int choice = scanner.nextInt();

        if (choice == 1) {
            // Register a new user
            System.out.println("Enter username: ");
            String username = scanner.next();
            System.out.println("Enter password: ");
            String password = scanner.next();
            authService.register(username, password);
        } else if (choice == 2) {
            // Login
            System.out.println("Enter username: ");
            String username = scanner.next();
            System.out.println("Enter password: ");
            String password = scanner.next();

            boolean loggedIn = authService.login(username, password);
            if (loggedIn) {
                System.out.println("Login successful!");

                // Example Quiz Interaction
                System.out.println("Available quizzes:");
                List<Quiz> quizzes = quizService.getAllQuizzes();
                for (Quiz quiz : quizzes) {
                    System.out.println(quiz.getTitle());
                }

                System.out.println("Select quiz ID to take: ");
                int quizId = scanner.nextInt();
                List<Question> questions = quizService.getQuestions(quizId);
                int score = 0;

                for (Question question : questions) {
                    System.out.println(question.getQuestionText());
                    for (String option : question.getOptions()) {
                        System.out.println(option);
                    }
                    System.out.println("Enter your answer: ");
                    String answer = scanner.next();
                    if (question.getCorrectAnswer().equals(answer)) {
                        score++;
                    }
                }

                System.out.println("Your score: " + score);
                progressService.saveResults(username, quizId, score);
            } else {
                System.out.println("Invalid credentials.");
            }
        }
    }

    // Database connection method
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// User class representing a user
class User {
    private int id;
    private String username;
    private String passwordHash;
    private String salt;

    // Getters and setters...
}

// AuthService for user authentication (register/login)
class AuthService {
    
    public void register(String username, String password) {
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);
            
            String query = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
            try (Connection conn = QuizApp.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);
                stmt.executeUpdate();
                System.out.println("User registered successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
        }
    }

    public boolean login(String username, String password) {
        String query = "SELECT password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = QuizApp.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                return storedHash.equals(hashPassword(password, salt));
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return false;
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}

// Quiz class representing a quiz
class Quiz {
    private int id;
    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    // Getters and setters...
}

// QuizService for managing quizzes
class QuizService {
    
    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        // Simulate fetching quizzes from database (could be enhanced to fetch from DB)
        quizzes.add(new Quiz()); // Add some quizzes for the demo
        return quizzes;
    }

    public List<Question> getQuestions(int quizId) {
        List<Question> questions = new ArrayList<>();
        // Simulate fetching questions (could be enhanced with DB query)
        questions.add(new Question(quizId, "What is 2 + 2?", "4", List.of("3", "4", "5")));
        return questions;
    }
}

// Question class representing a quiz question
class Question {
    private int quizId;
    private String questionText;
    private String correctAnswer;
    private List<String> options;

    public Question(int quizId, String questionText, String correctAnswer, List<String> options) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }
}

// ProgressService for tracking user progress
class ProgressService {
    
    public void saveResults(String username, int quizId, int score) {
        try {
            String query = "INSERT INTO user_quiz_results (username, quiz_id, score) VALUES (?, ?, ?)";
            try (Connection conn = QuizApp.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setInt(2, quizId);
                stmt.setInt(3, score);
                stmt.executeUpdate();
                System.out.println("Your results have been saved.");
            }
        } catch (SQLException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }
}
