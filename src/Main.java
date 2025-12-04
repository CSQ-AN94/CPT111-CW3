import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;

/**
 * Main类 - 程序入口
 */
public class Main {
    private static final String MOVIES_FILE = "data/movies.csv";
    private static final String USERS_FILE = "data/users.csv";

    private static ArrayList<Movie> allMovies;
    private static ArrayList<User> allUsers;
    private static User currentUser = null;

    private static MovieFileHandler movieHandler = new MovieFileHandler();
    private static UserFileHandler userHandler = new UserFileHandler();

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Welcome to Movie Recommendation System ===");
        System.out.println("Loading data...");

        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        if (allMovies.isEmpty() || allUsers.isEmpty()) {
            System.out.println("Error: Failed to load data. Please check data/movies.csv and data/users.csv");
            return;
        }

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showLoginMenu();
            } else {
                running = showMainMenu();
            }
        }

        System.out.println("Saving data...");
        userHandler.saveUsers(USERS_FILE, allUsers);
        System.out.println("Thank you for using Movie Recommendation System!");
        scanner.close();
    }

    private static boolean showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("3. Register");
        System.out.print("Please select an option: ");

        try {
            String input = scanner.nextLine().trim();
            if(input.isEmpty()) return true;
            int choice = Integer.parseInt(input);

            switch (choice) {
                case 1: login(); break;
                case 2: return false;
                case 3:
                    UserRegistration registrationService = new UserRegistration(scanner);
                    User newlyRegisteredUser = registrationService.registerNewUser(allUsers);
                    if (newlyRegisteredUser != null) {
                        // 1. 将新用户添加到内存列表
                        allUsers.add(newlyRegisteredUser);
                        // 写入csv
                        userHandler.saveUsers(USERS_FILE, allUsers);
                        //  立即登录新用户
                        currentUser = newlyRegisteredUser;
                        System.out.println("You are now logged in as " + currentUser.getUsername());
                    }

                    break;
                default: System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        return true;
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = userHandler.findUserByUsername(allUsers, username);

        if (user != null && user.verifyPassword(password)) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + username + "!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private static boolean showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Browse movies");
        System.out.println("2. Add movie to watchlist");
        System.out.println("3. Remove movie from watchlist");
        System.out.println("4. View watchlist");
        System.out.println("5. Mark movie as watched");
        System.out.println("6. View history");
        System.out.println("7. Get recommendations");
        System.out.println("8. Logout");
        System.out.print("Please select an option: ");

        try {
            String input = scanner.nextLine().trim();
            if(input.isEmpty()) return true;
            int choice = Integer.parseInt(input);

            switch (choice) {
                case 1: browseMovies(); break;
                case 2: addToWatchlist(); break;
                case 3: removeFromWatchlist(); break;
                case 4: viewWatchlist(); break;
                case 5: markAsWatched(); break;
                case 6: viewHistory(); break;
                case 7: getRecommendations(); break;
                case 8: logout(); break;
                default: System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
        return true;
    }

    private static void browseMovies() {
        System.out.println("\n=== All Movies ===");
        for (Movie movie : allMovies) {
            System.out.println(movie);
        }
    }

    private static void addToWatchlist() {
        System.out.print("Enter movie ID to add to watchlist (e.g., M001): ");
        try {
            String movieId = scanner.nextLine().trim();
            Movie movie = movieHandler.findMovieById(allMovies, movieId);

            if (movie == null) {
                System.out.println("Movie not found.");
            } else if (currentUser.getWatchlist().contains(movieId)) {
                System.out.println("Movie already in your watchlist.");
            } else {
                currentUser.addToWatchlist(movieId);
                System.out.println("Added '" + movie.getTitle() + "' to your watchlist.");
                userHandler.saveUsers(USERS_FILE, allUsers);
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private static void removeFromWatchlist() {
        if (currentUser.getWatchlist().isEmpty()) {
            System.out.println("Your watchlist is empty.");
            return;
        }
        viewWatchlist();
        System.out.print("Enter movie ID to remove: ");
        try {
            String movieId = scanner.nextLine().trim();
            if (currentUser.getWatchlist().contains(movieId)) {
                currentUser.removeFromWatchlist(movieId);
                System.out.println("Removed movie.");
                userHandler.saveUsers(USERS_FILE, allUsers);
            } else {
                System.out.println("Movie not in watchlist.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private static void viewWatchlist() {
        System.out.println("\n=== Your Watchlist ===");
        if (currentUser.getWatchlist().isEmpty()) {
            System.out.println("Your watchlist is empty.");
        } else {
            for (String movieId : currentUser.getWatchlist()) {
                Movie movie = movieHandler.findMovieById(allMovies, movieId);
                if (movie != null) System.out.println(movie);
            }
        }
    }

    private static void markAsWatched() {
        System.out.print("Enter movie ID to mark as watched: ");
        try {
            String movieId = scanner.nextLine().trim();
            Movie movie = movieHandler.findMovieById(allMovies, movieId);

            if (movie == null) {
                System.out.println("Movie not found.");
            } else {
                String currentDate = LocalDate.now().toString();
                currentUser.addToHistory(movieId, currentDate);
                System.out.println("Marked '" + movie.getTitle() + "' as watched.");
                userHandler.saveUsers(USERS_FILE, allUsers);
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private static void viewHistory() {
        System.out.println("\n=== Your Watch History ===");
        if (currentUser.getHistory().isEmpty()) {
            System.out.println("No history yet.");
        } else {
            for (String entry : currentUser.getHistory()) {
                String[] parts = entry.split("@");
                if (parts.length == 2) {
                    Movie movie = movieHandler.findMovieById(allMovies, parts[0]);
                    if (movie != null) {
                        System.out.println(movie + " | Watched: " + parts[1]);
                    }
                }
            }
        }
    }

    private static void getRecommendations() {
        System.out.print("How many recommendations do you want? ");
        try {
            String input = scanner.nextLine().trim();
            if(input.isEmpty()) return;
            int n = Integer.parseInt(input);
            if (n <= 0) {
                System.out.println("Please enter a positive number.");
                return;
            }

            // 改动点：模拟 GUI 的“复选框”逻辑
            System.out.println("\n--- Select Features to Analyze ---");
            System.out.println("1. Genre  (Analyze your favorite types)");
            System.out.println("2. Year   (Analyze your preferred era)");
            System.out.println("3. Rating (Analyze your quality preference)");
            System.out.println("Enter the numbers you want to include (e.g., '1,3' for Genre + Rating).");
            System.out.print("Press Enter directly to select ALL (Recommended): ");

            boolean useGenre = true;
            boolean useYear = true;
            boolean useRating = true;

            String selection = scanner.nextLine().trim();

            // 如果用户输入了内容，则根据输入决定开关
            if (!selection.isEmpty()) {
                // 初始化为全 false，根据输入开启
                useGenre = selection.contains("1");
                useYear = selection.contains("2");
                useRating = selection.contains("3");

                // 防呆设计：如果用户输入了 "abc" 或者没选任何有效数字，默认全开
                if (!useGenre && !useYear && !useRating) {
                    System.out.println("No valid selection detected. Defaulting to ALL features.");
                    useGenre = true;
                    useYear = true;
                    useRating = true;
                }
            }
            // 如果用户直接回车，保持默认的全 true

            RecommendationEngine engine = new RecommendationEngine(allMovies);
            ArrayList<RecommendationEngine.MovieScore> recommendations =
                    engine.getRecommendations(currentUser, n, useGenre, useYear, useRating);

            System.out.println("\n=== Recommended Movies for You ===");
            if (recommendations.isEmpty()) {
                System.out.println("No recommendations available.");
            } else {
                for (RecommendationEngine.MovieScore ms : recommendations) {
                    double displayScore = ms.score * 10.0;
                    System.out.println(String.format("[Score: %.2f] %s", displayScore, ms.movie.toString()));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void logout() {
        System.out.println("Logging out... Goodbye, " + currentUser.getUsername() + "!");
        currentUser = null;
    }
}