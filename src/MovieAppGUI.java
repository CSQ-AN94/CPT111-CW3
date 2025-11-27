import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * 图形界面版本的电影推荐系统
 */
public class MovieAppGUI extends Application {

    // 文件路径（和 Main.java 保持一致）
    private static final String MOVIES_FILE = "data/movies.csv";
    private static final String USERS_FILE = "data/users.csv";

    // 数据存储
    private ArrayList<Movie> allMovies;
    private ArrayList<User> allUsers;
    private User currentUser;

    // 文件处理类
    private MovieFileHandler movieHandler = new MovieFileHandler();
    private UserFileHandler userHandler = new UserFileHandler();

    // JavaFX 组件
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;

    private ListView<Movie> movieListView;          // 显示所有电影
    private ListView<Movie> watchlistView;          // 显示 watchlist
    private ListView<String> historyView;           // 显示 history
    private TextArea messageArea;                   // 显示提示信息 / 推荐结果

    public static void main(String[] args) {
        launch(args);   // 启动 JavaFX
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. 加载数据
        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        // 2. 构建两个场景：登录界面 + 主界面
        this.loginScene = buildLoginScene();
        this.mainScene = buildMainScene();

        // 3. 初始显示登录界面（窗口稍微小一点）
        primaryStage.setTitle("Movie Recommendation System (GUI)");
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.centerOnScreen();

        // 给一点最小尺寸，防止被缩得太夸张
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(450);

        // 4. 关闭窗口时保存数据
        primaryStage.setOnCloseRequest(event -> {
            if (allUsers != null) {
                userHandler.saveUsers(USERS_FILE, allUsers);
            }
        });

        primaryStage.show();
    }

    /**
     * 构建登录界面
     */
    private Scene buildLoginScene() {
        Label titleLabel = new Label("Movie Recommendation System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Label infoLabel = new Label();   // 提示错误信息

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                infoLabel.setText("Username and password cannot be empty.");
                return;
            }

            User user = userHandler.findUserByUsername(allUsers, username);
            if (user == null) {
                infoLabel.setText("User not found.");
                return;
            }

            if (!user.verifyPassword(password)) {
                infoLabel.setText("Incorrect password.");
                return;
            }

            // 登录成功
            currentUser = user;
            infoLabel.setText("");
            userField.clear();
            passField.clear();

            refreshAllViews();

            // 切换到主界面时放大一点，适合显示三个列表
            primaryStage.setTitle("Movie System - " + currentUser.getUsername());
            primaryStage.setScene(mainScene);
            primaryStage.setWidth(1100);
            primaryStage.setHeight(700);
            primaryStage.centerOnScreen();

            // 主界面最小尺寸稍微大一点
            primaryStage.setMinWidth(950);
            primaryStage.setMinHeight(600);
        });

        VBox vbox = new VBox(10, titleLabel, userLabel, userField,
                passLabel, passField, loginButton, infoLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(40));

        return new Scene(vbox);
    }

    /**
     * 构建主界面
     */
    private Scene buildMainScene() {
        // ----- 左边：电影列表 -----
        movieListView = new ListView<>();
        movieListView.setPlaceholder(new Label("No movies loaded."));
        movieListView.setItems(FXCollections.observableArrayList(allMovies));

        TitledPane allMoviesPane = new TitledPane("All Movies", movieListView);
        allMoviesPane.setCollapsible(false);
        allMoviesPane.setPrefWidth(650); // 左边稍宽一点

        // ----- 右上：Watchlist -----
        watchlistView = new ListView<>();
        watchlistView.setPlaceholder(new Label("Watchlist is empty."));
        TitledPane watchlistPane = new TitledPane("Watchlist", watchlistView);
        watchlistPane.setCollapsible(false);

        // ----- 右下：History -----
        historyView = new ListView<>();
        historyView.setPlaceholder(new Label("No history yet."));
        TitledPane historyPane = new TitledPane("History", historyView);
        historyPane.setCollapsible(false);

        VBox rightBox = new VBox(10, watchlistPane, historyPane);
        rightBox.setPrefWidth(380);
        VBox.setVgrow(watchlistPane, Priority.SOMETIMES);
        VBox.setVgrow(historyPane, Priority.SOMETIMES);

        // ----- 底部：按钮 + 消息区域 -----
        Button addToWatchlistBtn = new Button("Add to Watchlist");
        addToWatchlistBtn.setOnAction(e -> handleAddToWatchlist());

        Button removeFromWatchlistBtn = new Button("Remove from Watchlist");
        removeFromWatchlistBtn.setOnAction(e -> handleRemoveFromWatchlist());

        Button markWatchedBtn = new Button("Mark as Watched");
        markWatchedBtn.setOnAction(e -> handleMarkAsWatched());

        Button recommendBtn = new Button("Get Recommendations");
        recommendBtn.setOnAction(e -> handleRecommendations());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> handleLogout());

        HBox buttonBox = new HBox(10,
                addToWatchlistBtn,
                removeFromWatchlistBtn,
                markWatchedBtn,
                recommendBtn,
                logoutBtn
        );
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(6); // 稍微高一点，方便看推荐列表

        VBox bottomBox = new VBox(5, buttonBox, messageArea);
        bottomBox.setPadding(new Insets(10));

        // ----- 总布局 -----
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setLeft(allMoviesPane);
        root.setCenter(rightBox);
        root.setBottom(bottomBox);
        BorderPane.setMargin(allMoviesPane, new Insets(0, 10, 0, 0));

        return new Scene(root);
    }

    /**
     * 刷新所有界面列表（登录成功 / 数据变化后调用）
     */
    private void refreshAllViews() {
        if (currentUser == null) return;
        refreshMovieListView();
        refreshWatchlistView();
        refreshHistoryView();
    }

    private void refreshMovieListView() {
        if (movieListView != null && allMovies != null) {
            ObservableList<Movie> list = FXCollections.observableArrayList(allMovies);
            movieListView.setItems(list);
        }
    }

    private void refreshWatchlistView() {
        if (currentUser == null || watchlistView == null) return;

        ArrayList<String> watchIds = currentUser.getWatchlist();
        ArrayList<Movie> watchMovies = new ArrayList<>();

        for (String id : watchIds) {
            Movie m = movieHandler.findMovieById(allMovies, id);
            if (m != null) {
                watchMovies.add(m);
            }
        }

        watchlistView.setItems(FXCollections.observableArrayList(watchMovies));
    }

    private void refreshHistoryView() {
        if (currentUser == null || historyView == null) return;

        ArrayList<String> history = currentUser.getHistory();
        ObservableList<String> display = FXCollections.observableArrayList();

        for (String entry : history) {
            // entry 格式：M001@2025-07-12
            String[] parts = entry.split("@");
            if (parts.length == 2) {
                String movieId = parts[0];
                String date = parts[1];
                Movie m = movieHandler.findMovieById(allMovies, movieId);
                if (m != null) {
                    display.add(m.getTitle() + " (" + date + ")");
                }
            }
        }

        historyView.setItems(display);
    }

    private void showMessage(String msg) {
        messageArea.appendText(msg + "\n");
    }

    // ================= 按钮事件 =================

    /**
     * 按钮：添加到 Watchlist
     */
    private void handleAddToWatchlist() {
        if (currentUser == null) {
            showMessage("Please login first.");
            return;
        }
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a movie from 'All Movies'.");
            return;
        }

        String movieId = selected.getId();
        if (currentUser.getWatchlist().contains(movieId)) {
            showMessage("Movie already in your watchlist.");
        } else if (currentUser.getWatchedMovieIds().contains(movieId)) {
            showMessage("You have already watched this movie.");
        } else {
            currentUser.addToWatchlist(movieId);
            refreshWatchlistView();
            showMessage("Added '" + selected.getTitle() + "' to watchlist.");
            userHandler.saveUsers(USERS_FILE, allUsers);
        }
    }

    /**
     * 按钮：从 Watchlist 移除
     */
    private void handleRemoveFromWatchlist() {
        if (currentUser == null) {
            showMessage("Please login first.");
            return;
        }
        Movie selected = watchlistView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a movie from 'Watchlist'.");
            return;
        }

        String movieId = selected.getId();
        if (currentUser.getWatchlist().contains(movieId)) {
            currentUser.removeFromWatchlist(movieId);
            refreshWatchlistView();
            showMessage("Removed '" + selected.getTitle() + "' from watchlist.");
            userHandler.saveUsers(USERS_FILE, allUsers);
        } else {
            showMessage("Movie not found in your watchlist.");
        }
    }

    /**
     * 按钮：标记为已观看
     */
    private void handleMarkAsWatched() {
        if (currentUser == null) {
            showMessage("Please login first.");
            return;
        }
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Please select a movie from 'All Movies'.");
            return;
        }

        String movieId = selected.getId();
        if (currentUser.getWatchedMovieIds().contains(movieId)) {
            showMessage("You have already watched this movie.");
            return;
        }

        String today = LocalDate.now().toString();
        currentUser.addToHistory(movieId, today);   // 会自动从 watchlist 中移除
        refreshWatchlistView();
        refreshHistoryView();
        showMessage("Marked '" + selected.getTitle() + "' as watched on " + today + ".");
        userHandler.saveUsers(USERS_FILE, allUsers);
    }

    /**
     * 按钮：获取推荐
     */
    private void handleRecommendations() {
        if (currentUser == null) {
            showMessage("Please login first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Recommendations");
        dialog.setHeaderText("How many recommendations do you want?");
        dialog.setContentText("N:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int n = Integer.parseInt(input.trim());
                if (n <= 0) {
                    showMessage("Please enter a positive number.");
                    return;
                }

                RecommendationEngine engine = new RecommendationEngine(allMovies);

// getRecommendations 现在返回的是 MovieScore 列表
                ArrayList<RecommendationEngine.MovieScore> recs =
                        engine.getRecommendations(currentUser, n);

                showMessage("=== Recommended Movies ===");
                if (recs.isEmpty()) {
                    showMessage("No recommendations available.");
                } else {
                    for (RecommendationEngine.MovieScore item : recs) {
                        // 下面两行根据你 MovieScore 的写法调整：
                        // 如果是 public 字段：movie / score
                        Movie m = item.movie;
                        double s = item.score;

                        // 如果是 getter，就写成：
                        // Movie m = item.getMovie();
                        // double s = item.getScore();

                        showMessage(String.format("%s (Score: %.2f)", m.getTitle(), s));
                    }
                }
            } catch (NumberFormatException ex) {
                showMessage("Invalid number.");
            }
        });
    }

    /**
     * 按钮：注销
     */
    private void handleLogout() {
        if (currentUser != null) {
            userHandler.saveUsers(USERS_FILE, allUsers);
            showMessage("User data saved.");
        }
        currentUser = null;
        primaryStage.setTitle("Movie Recommendation System (GUI)");
        primaryStage.setScene(loginScene);

        // 回到登录界面时也把窗口尺寸收回正常一点
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.centerOnScreen();

        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(450);
    }
}
