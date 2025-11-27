import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main类 - 程序入口，处理用户交互
 */
public class Main {
    // 文件路径
    private static final String MOVIES_FILE = "data/movies.csv";
    private static final String USERS_FILE = "data/users.csv";

    // 数据存储
    private static ArrayList<Movie> allMovies;
    private static ArrayList<User> allUsers;
    private static User currentUser = null;  // 当前登录的用户

    // 文件处理器
    private static MovieFileHandler movieHandler = new MovieFileHandler();
    private static UserFileHandler userHandler = new UserFileHandler();

    // Scanner用于读取用户输入
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // 启动时加载数据
        System.out.println("=== Welcome to Movie Recommendation System ===");
        System.out.println("Loading data...");

        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        if (allMovies.isEmpty() || allUsers.isEmpty()) {
            System.out.println("Error: Failed to load data. Please check your data files.");
            return;
        }

        // 主循环
        boolean running = true;
        while (running) {
            if (currentUser == null) {
                // 未登录状态菜单
                running = showLoginMenu();
            } else {
                // 已登录状态菜单
                running = showMainMenu();
            }
        }

        // 程序退出前保存数据
        System.out.println("Saving data...");
        userHandler.saveUsers(USERS_FILE, allUsers);
        System.out.println("Thank you for using Movie Recommendation System!");
        scanner.close();
    }

    /**
     * 显示登录菜单（未登录状态）
     * @return 是否继续运行程序
     */
    private static boolean showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.print("Please select an option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    return false;  // 退出程序
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }

        return true;
    }

    /**
     * 登录功能
     */
    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // 查找用户
        User user = userHandler.findUserByUsername(allUsers, username);

        if (user != null && user.verifyPassword(password)) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + username + "!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    /**
     * 显示主菜单（已登录状态）
     * @return 是否继续运行程序
     */
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
            int choice = Integer.parseInt(scanner.nextLine().trim());

            switch (choice) {
                case 1:
                    browseMovies();
                    break;
                case 2:
                    addToWatchlist();
                    break;
                case 3:
                    removeFromWatchlist();
                    break;
                case 4:
                    viewWatchlist();
                    break;
                case 5:
                    markAsWatched();
                    break;
                case 6:
                    viewHistory();
                    break;
                case 7:
                    getRecommendations();
                    break;
                case 8:
                    logout();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }

        return true;
    }

    /**
     * 浏览所有电影
     */
    private static void browseMovies() {
        System.out.println("\n=== All Movies ===");
        for (Movie movie : allMovies) {
            System.out.println(movie);
        }
    }

    /**
     * 添加电影到观看列表
     */
    private static void addToWatchlist() {
        System.out.print("Enter movie ID to add to watchlist (e.g., M001): ");
        try {
            String movieId = scanner.nextLine().trim();
            Movie movie = movieHandler.findMovieById(allMovies, movieId);

            if (movie == null) {
                System.out.println("Movie not found.");
            } else if (currentUser.getWatchlist().contains(movieId)) {
                System.out.println("Movie already in your watchlist.");
            } else if (currentUser.getWatchedMovieIds().contains(movieId)) {
                System.out.println("You have already watched this movie.");
            } else {
                currentUser.addToWatchlist(movieId);
                System.out.println("Added '" + movie.getTitle() + "' to your watchlist.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid movie ID.");
        }
    }

    /**
     * 从观看列表移除电影
     */
    private static void removeFromWatchlist() {
        if (currentUser.getWatchlist().isEmpty()) {
            System.out.println("Your watchlist is empty.");
            return;
        }

        viewWatchlist();
        System.out.print("Enter movie ID to remove from watchlist (e.g., M001): ");

        try {
            String movieId = scanner.nextLine().trim();
            if (currentUser.getWatchlist().contains(movieId)) {
                currentUser.removeFromWatchlist(movieId);
                System.out.println("Removed movie from your watchlist.");
            } else {
                System.out.println("Movie not in your watchlist.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid movie ID.");
        }
    }

    /**
     * 查看观看列表
     */
    private static void viewWatchlist() {
        System.out.println("\n=== Your Watchlist ===");
        if (currentUser.getWatchlist().isEmpty()) {
            System.out.println("Your watchlist is empty.");
        } else {
            for (String movieId : currentUser.getWatchlist()) {
                Movie movie = movieHandler.findMovieById(allMovies, movieId);
                if (movie != null) {
                    System.out.println(movie);
                }
            }
        }
    }

    /**
     * 标记电影为已观看
     */
    private static void markAsWatched() {
        System.out.print("Enter movie ID to mark as watched (e.g., M001): ");
        try {
            String movieId = scanner.nextLine().trim();
            Movie movie = movieHandler.findMovieById(allMovies, movieId);

            if (movie == null) {
                System.out.println("Movie not found.");
            } else if (currentUser.getWatchedMovieIds().contains(movieId)) {
                System.out.println("You have already watched this movie.");
            } else {
                // 获取当前日期
                String currentDate = java.time.LocalDate.now().toString();
                currentUser.addToHistory(movieId, currentDate);
                System.out.println("Marked '" + movie.getTitle() + "' as watched.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid movie ID.");
        }
    }

    /**
     * 查看观看历史
     */
    private static void viewHistory() {
        System.out.println("\n=== Your Watch History ===");
        if (currentUser.getHistory().isEmpty()) {
            System.out.println("You haven't watched any movies yet.");
        } else {
            for (String entry : currentUser.getHistory()) {
                // 解析格式：M001@2025-07-12
                String[] parts = entry.split("@");
                if (parts.length == 2) {
                    String movieId = parts[0];
                    String date = parts[1];
                    Movie movie = movieHandler.findMovieById(allMovies, movieId);
                    if (movie != null) {
                        System.out.println(movie + " | Watched on: " + date);
                    }
                }
            }
        }
    }

    /**
     * 获取电影推荐
     */
    private static void getRecommendations() {
        System.out.print("How many recommendations do you want? ");
        try {
            int n = Integer.parseInt(scanner.nextLine().trim());

            if (n <= 0) {
                System.out.println("Please enter a positive number.");
                return;
            }

            RecommendationEngine engine = new RecommendationEngine(allMovies);
            ArrayList<RecommendationEngine.MovieScore> recommendations = engine.getRecommendations(currentUser, n);

            System.out.println("\n=== Recommended Movies for You ===");
            if (recommendations.isEmpty()) {
                System.out.println("No recommendations available.");
            } else {
                for (RecommendationEngine.MovieScore movie : recommendations) {
                    System.out.println(movie);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    /**
     * 登出
     */
    private static void logout() {
        System.out.println("Logging out... Goodbye, " + currentUser.getUsername() + "!");
        currentUser = null;
    }
}