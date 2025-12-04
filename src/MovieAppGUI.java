import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * 图形界面版本的电影推荐系统 (高级推荐策略版)
 *
 * 功能更新：
 * 1. [高级推荐] handleRecommendations() 现在会弹出一个包含复选框(CheckBox)的窗口，
 * 允许用户自定义是否启用 Genre/Year/Rating 策略。
 * 2. [GUI组件] 使用了 CheckBox, VBox, Stage 等组件，符合 Lecture 12 规范。
 * 3. [原有功能] 保留了注册、登录、观看列表、历史记录等所有功能。
 */
public class MovieAppGUI extends Application {

    // 文件路径
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
    private Scene registerScene;
    private Scene mainScene;

    // 主界面组件引用
    private ListView<Movie> movieListView;
    private ListView<Movie> watchlistView;
    private ListView<String> historyView;
    private TextArea messageArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. 加载数据
        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        // 2. 构建场景
        this.loginScene = buildLoginScene();
        this.registerScene = buildRegisterScene();
        this.mainScene = buildMainScene();

        // 3. 设置初始舞台
        primaryStage.setTitle("Movie Recommendation System");
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // 4. 关闭窗口事件
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (allUsers != null) {
                    userHandler.saveUsers(USERS_FILE, allUsers);
                }
            }
        });

        primaryStage.show();
    }

    /**
     * === 场景 1: 登录界面 ===
     */
    private Scene buildLoginScene() {
        Label titleLabel = new Label("Movie Recommendation System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(100);

        Button goToRegisterButton = new Button("Register New User");
        goToRegisterButton.setPrefWidth(150);

        // 登录事件
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String username = userField.getText().trim();
                String password = passField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    infoLabel.setText("Username and password cannot be empty.");
                    return;
                }

                User user = userHandler.findUserByUsername(allUsers, username);
                if (user == null) {
                    infoLabel.setText("User not found. Please register.");
                    return;
                }

                if (!user.verifyPassword(password)) {
                    infoLabel.setText("Incorrect password.");
                    return;
                }

                currentUser = user;
                infoLabel.setText("");
                userField.clear();
                passField.clear();

                refreshAllViews();
                primaryStage.setTitle("Movie System - " + currentUser.getUsername());
                primaryStage.setScene(mainScene);
            }
        });

        // 跳转注册事件
        goToRegisterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                infoLabel.setText("");
                primaryStage.setScene(registerScene);
                primaryStage.setTitle("Register New User");
            }
        });

        VBox layout = new VBox(15);
        layout.getChildren().addAll(
                titleLabel,
                userLabel,
                userField,
                passLabel,
                createPasswordWithEye(passField), // <--- 这里调用新方法
                loginButton,
                goToRegisterButton,
                infoLabel
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        userField.setMaxWidth(300);
        passField.setMaxWidth(300);

        return new Scene(layout);
    }

    /**
     * === 场景 2: 注册界面 ===
     */
    private Scene buildRegisterScene() {
        Label headerLabel = new Label("Create New Account");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label userLbl = new Label("Username:");
        TextField userFld = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passFld = new PasswordField();
        Label confirmLbl = new Label("Confirm Password:");
        PasswordField confirmFld = new PasswordField();

        grid.add(userLbl, 0, 0); grid.add(userFld, 1, 0);
        grid.add(passLbl, 0, 1);
        grid.add(createPasswordWithEye(passFld), 1, 1); // <--- 使用带眼睛的密码框

        grid.add(confirmLbl, 0, 2);
        grid.add(createPasswordWithEye(confirmFld), 1, 2); // <--- 确认密码也加上

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: red;");

        Button registerBtn = new Button("Register & Login");
        Button backBtn = new Button("Back to Login");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerBtn, backBtn);

        // 注册事件
        registerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String uName = userFld.getText().trim();
                String pwd = passFld.getText().trim();
                String confirm = confirmFld.getText().trim();

                if (uName.isEmpty() || pwd.isEmpty()) {
                    errorMsg.setText("All fields required.");
                    return;
                }
                if (!pwd.equals(confirm)) {
                    errorMsg.setText("Passwords do not match.");
                    return;
                }
                if (userHandler.findUserByUsername(allUsers, uName) != null) {
                    errorMsg.setText("Username already taken.");
                    return;
                }

                User newUser = new User(uName, pwd);
                allUsers.add(newUser);
                userHandler.saveUsers(USERS_FILE, allUsers);

                currentUser = newUser;
                refreshAllViews();

                userFld.clear(); passFld.clear(); confirmFld.clear(); errorMsg.setText("");
                primaryStage.setTitle("Movie System - " + currentUser.getUsername());
                primaryStage.setScene(mainScene);
                showMessage("Welcome, " + uName + "! Registration successful.");
            }
        });

        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                userFld.clear(); passFld.clear(); confirmFld.clear(); errorMsg.setText("");
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("Movie Recommendation System");
            }
        });

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(headerLabel, grid, errorMsg, buttonBox);

        return new Scene(layout);
    }

    /**
     * === 场景 3: 主功能界面 ===
     */
    private Scene buildMainScene() {
        // 左：电影列表
        movieListView = new ListView<>();
        movieListView.setPlaceholder(new Label("No movies loaded."));
        if (allMovies != null) {
            movieListView.setItems(FXCollections.observableArrayList(allMovies));
        }
        TitledPane allMoviesPane = new TitledPane("All Movies", movieListView);
        allMoviesPane.setCollapsible(false);
        allMoviesPane.setMaxHeight(Double.MAX_VALUE);

        // 右上：待看列表
        watchlistView = new ListView<>();
        watchlistView.setPlaceholder(new Label("Watchlist is empty."));
        TitledPane watchlistPane = new TitledPane("Watchlist", watchlistView);
        watchlistPane.setCollapsible(false);
        watchlistPane.setMaxHeight(Double.MAX_VALUE);

        // 右下：历史记录
        historyView = new ListView<>();
        historyView.setPlaceholder(new Label("No history yet."));
        TitledPane historyPane = new TitledPane("History", historyView);
        historyPane.setCollapsible(false);
        historyPane.setMaxHeight(Double.MAX_VALUE);

        VBox rightBox = new VBox(10);
        rightBox.getChildren().addAll(watchlistPane, historyPane);
        rightBox.setPrefWidth(350);
        VBox.setVgrow(watchlistPane, Priority.ALWAYS);
        VBox.setVgrow(historyPane, Priority.ALWAYS);

        // 底部按钮
        Button addToWatchlistBtn = new Button("Add to Watchlist");
        addToWatchlistBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleAddToWatchlist(); }});

        Button removeFromWatchlistBtn = new Button("Remove from Watchlist");
        removeFromWatchlistBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleRemoveFromWatchlist(); }});

        Button markWatchedBtn = new Button("Mark as Watched");
        markWatchedBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleMarkAsWatched(); }});

        Button recommendBtn = new Button("Get Recommendations");
        recommendBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleRecommendations(); }});

        Button changePassBtn = new Button("Change Password");
        changePassBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleChangePassword(); }});

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleLogout(); }});

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addToWatchlistBtn, removeFromWatchlistBtn, markWatchedBtn, recommendBtn, changePassBtn, logoutBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(5);

        VBox bottomBox = new VBox(5);
        bottomBox.getChildren().addAll(buttonBox, messageArea);
        bottomBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setLeft(allMoviesPane);
        root.setCenter(rightBox);
        root.setBottom(bottomBox);
        BorderPane.setMargin(allMoviesPane, new Insets(0, 10, 0, 0));

        return new Scene(root);
    }

    private void refreshAllViews() {
        if (currentUser == null) return;
        if (movieListView != null && allMovies != null) movieListView.setItems(FXCollections.observableArrayList(allMovies));
        refreshWatchlistView();
        refreshHistoryView();
    }

    private void refreshWatchlistView() {
        if (currentUser == null || watchlistView == null) return;
        ArrayList<String> watchIds = currentUser.getWatchlist();
        ArrayList<Movie> watchMovies = new ArrayList<>();
        for (String id : watchIds) {
            Movie m = movieHandler.findMovieById(allMovies, id);
            if (m != null) watchMovies.add(m);
        }
        watchlistView.setItems(FXCollections.observableArrayList(watchMovies));
    }


    /*private void refreshHistoryView() {
        if (currentUser == null || historyView == null) return;
        ArrayList<String> history = currentUser.getHistory();
        ObservableList<String> display = FXCollections.observableArrayList();
        for (int i = history.size() - 1; i >= 0; i--) {
            String entry = history.get(i);
            String[] parts = entry.split("@");
            if (parts.length == 2) {
                Movie m = movieHandler.findMovieById(allMovies, parts[0]);
                if (m != null) display.add(m.getTitle() + " (" + parts[1] + ")");
            }
        }
        historyView.setItems(display);
    }*/
    private void refreshHistoryView() {
        if (currentUser == null || historyView == null) return;

        ArrayList<String> history = currentUser.getHistory();
        ObservableList<String> display = FXCollections.observableArrayList();

        // 倒序显示，最新的在最上面
        for (int i = history.size() - 1; i >= 0; i--) {
            String entry = history.get(i);
            String[] parts = entry.split("@");
            if (parts.length == 2) {
                String movieId = parts[0];
                String date = parts[1]; // 日期部分 (YYYY-MM-DD)

                Movie m = movieHandler.findMovieById(allMovies, movieId);
                if (m != null) {
                    // [关键修改]
                    // 以前是: String.format("ID: %-5s | %-40s | Date: %s", ...)
                    // 现在改为: 直接调用 m.toString()，它会自动包含 Genre, Year, Rating 等所有信息
                    // 然后我们在最后加上 "| Date: ..." 即可
                    String formattedEntry = String.format("%s | Date: %s", m.toString(), date);

                    display.add(formattedEntry);
                }
            }
        }
        historyView.setItems(display);
    }

    private void showMessage(String msg) {
        if (messageArea != null) messageArea.appendText(msg + "\n");
    }

    // ================= 按钮功能实现 =================

    private void handleAddToWatchlist() {
        if (currentUser == null) return;
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Please select a movie from 'All Movies'."); return; }
        if (currentUser.getWatchlist().contains(selected.getId())) { showMessage("Already in watchlist."); }
        else {
            currentUser.addToWatchlist(selected.getId());
            refreshWatchlistView();
            showMessage("Added to watchlist: " + selected.getTitle());
            userHandler.saveUsers(USERS_FILE, allUsers);
        }
    }

    private void handleRemoveFromWatchlist() {
        if (currentUser == null) return;
        Movie selected = watchlistView.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Please select from Watchlist."); return; }
        currentUser.removeFromWatchlist(selected.getId());
        refreshWatchlistView();
        showMessage("Removed from watchlist: " + selected.getTitle());
        userHandler.saveUsers(USERS_FILE, allUsers);
    }

    /*private void handleMarkAsWatched() {
        if (currentUser == null) return;
        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        if (selected == null) selected = watchlistView.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Select a movie to mark as watched."); return; }

        currentUser.addToHistory(selected.getId(), LocalDate.now().toString());
        if (currentUser.getWatchlist().contains(selected.getId())) currentUser.removeFromWatchlist(selected.getId());

        refreshWatchlistView(); refreshHistoryView();
        showMessage("Marked as watched: " + selected.getTitle());
        userHandler.saveUsers(USERS_FILE, allUsers);
    }*/
    private void handleMarkAsWatched() {
        if (currentUser == null) return;

        Movie selected = movieListView.getSelectionModel().getSelectedItem();
        // 如果左侧没选，尝试看右侧 Watchlist 有没有选
        if (selected == null) {
            selected = watchlistView.getSelectionModel().getSelectedItem();
        }

        if (selected == null) {
            showMessage("Please select a movie to mark as watched.");
            return;
        }

        String movieId = selected.getId();

        // [重点符合 Lecture 11] 使用 java.time.LocalDate 获取当前日期
        // 不要使用 new java.util.Date()
        String today = java.time.LocalDate.now().toString();

        currentUser.addToHistory(movieId, today);

        // 如果在待看列表里，看完后移除
        if (currentUser.getWatchlist().contains(movieId)) {
            currentUser.removeFromWatchlist(movieId);
        }

        refreshWatchlistView();
        refreshHistoryView();
        showMessage("Marked as watched: " + selected.getTitle() + " on " + today);
        userHandler.saveUsers(USERS_FILE, allUsers);
    }

    /**
     * [关键修改] 处理推荐功能 - 弹出自定义设置窗口
     */
    private void handleRecommendations() {
        if (currentUser == null) return;

        // 1. 创建一个新的 Stage (弹出窗口)
        final Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL); // 阻止操作主窗口直到关闭此窗口
        dialogStage.setTitle("Recommendation Settings");

        // 2. 布局容器
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        // 3. 数量输入
        Label numLabel = new Label("How many movies?");
        TextField numField = new TextField("5");

        // 4. 复选框 - 允许用户选择推荐策略 (高级设计)
        Label criteriaLabel = new Label("Select Criteria to Analyze:");
        criteriaLabel.setStyle("-fx-font-weight: bold;");

        // CheckBox 是 Lecture 12 提到的控件
        CheckBox cbGenre = new CheckBox("Genre (Based on what types you watch)");
        CheckBox cbYear = new CheckBox("Year (Based on your preferred era)");
        CheckBox cbRating = new CheckBox("Rating (Based on high quality)");

        // 默认全选
        cbGenre.setSelected(true);
        cbYear.setSelected(true);
        cbRating.setSelected(true);

        // 5. 确认按钮
        Button btnConfirm = new Button("Get Recommendations");

        // 6. 按钮事件
        btnConfirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    // 获取输入
                    int n = Integer.parseInt(numField.getText().trim());
                    if (n <= 0) {
                        showMessage("Please enter a positive number.");
                        return;
                    }

                    // 获取复选框状态
                    boolean useGenre = cbGenre.isSelected();
                    boolean useYear = cbYear.isSelected();
                    boolean useRating = cbRating.isSelected();

                    // 防呆设计：如果都没选，默认全选
                    if (!useGenre && !useYear && !useRating) {
                        showMessage("No criteria selected. Defaulting to ALL.");
                        useGenre = true; useYear = true; useRating = true;
                    }

                    // 调用引擎
                    RecommendationEngine engine = new RecommendationEngine(allMovies);
                    ArrayList<RecommendationEngine.MovieScore> recs =
                            engine.getRecommendations(currentUser, n, useGenre, useYear, useRating);

                    // 在主界面显示结果
                    showMessage("\n=== Recommendations (G=" + useGenre + ", Y=" + useYear + ", R=" + useRating + ") ===");
                    if (recs.isEmpty()) {
                        showMessage("No recommendations available.");
                    } else {
                        for (RecommendationEngine.MovieScore ms : recs) {
                            showMessage(String.format("Score: %.1f | %s", ms.score * 10, ms.movie.toString()));
                        }
                    }

                    // 关闭弹出窗口
                    dialogStage.close();

                } catch (NumberFormatException ex) {
                    // 这里不用 System.out，而是弹窗或者在主界面提示，这里简单点用 Label 提示也可以，但为了代码简洁直接在主界面输出
                    showMessage("Invalid number input.");
                }
            }
        });

        layout.getChildren().addAll(numLabel, numField, new Separator(), criteriaLabel, cbGenre, cbYear, cbRating, new Separator(), btnConfirm);

        Scene dialogScene = new Scene(layout, 300, 350);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    private void handleChangePassword() {
        if (currentUser == null) return;
        Stage pwdStage = new Stage();
        pwdStage.setTitle("Change Password");
        VBox box = new VBox(10); box.setPadding(new Insets(20)); box.setAlignment(Pos.CENTER_LEFT);

        PasswordField oldF = new PasswordField(); oldF.setPromptText("Old Password");
        PasswordField newF = new PasswordField(); newF.setPromptText("New Password");
        PasswordField conF = new PasswordField(); conF.setPromptText("Confirm New Password");
        Label errL = new Label(); errL.setStyle("-fx-text-fill: red;");
        Button saveBtn = new Button("Save");

        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String newP = newF.getText();
                if (!currentUser.verifyPassword(oldF.getText())) errL.setText("Incorrect old password.");
                else if (newP.isEmpty()) errL.setText("Password cannot be empty.");
                else if (!newP.equals(conF.getText())) errL.setText("Passwords do not match.");
                else {
                    currentUser.setPassword(newP);
                    userHandler.saveUsers(USERS_FILE, allUsers);
                    showMessage("Password changed.");
                    pwdStage.close();
                }
            }
        });
        box.getChildren().addAll(new Label("Change Password"), oldF, newF, conF, errL, saveBtn);
        pwdStage.setScene(new Scene(box, 300, 300));
        pwdStage.show();
    }

    private void handleLogout() {
        userHandler.saveUsers(USERS_FILE, allUsers);
        currentUser = null;
        if (messageArea != null) messageArea.clear();
        primaryStage.setTitle("Movie Recommendation System");
        primaryStage.setScene(loginScene);
    }
    /**
     * [新增功能] 创建带"小眼睛"切换可见性的密码输入框组件
     * @param passField 已经创建好的 PasswordField 对象（用于保存密码值）
     * @return 包含输入框和切换按钮的布局节点 (StackPane)
     */
    private javafx.scene.layout.StackPane createPasswordWithEye(PasswordField passField) {
        // 1. 创建一个用于显示明文的 TextField
        TextField textField = new TextField();
        // 让两个框的内容始终同步（无论你在哪个框输入，另一个都会自动更新）
        textField.textProperty().bindBidirectional(passField.textProperty());
        textField.setPromptText(passField.getPromptText());

        // 2. 初始设置：隐藏明文框，显示密码框
        textField.setVisible(false);
        textField.setManaged(false); // 隐藏时不占位

        // 3. 创建"小眼睛"按钮
        Button toggleBtn = new Button("👁"); // 你也可以用图片或图标字体
        // 设置按钮样式：透明背景，鼠标变成手型
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
        toggleBtn.setFocusTraversable(false); // 防止 Tab 键焦点停在按钮上

        // 4. 按钮点击事件：切换显示/隐藏
        toggleBtn.setOnAction(e -> {
            boolean isVisible = textField.isVisible();
            if (isVisible) {
                // 切换回隐藏模式 (••••)
                textField.setVisible(false);
                textField.setManaged(false);
                passField.setVisible(true);
                passField.setManaged(true);
                toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: black;");
            } else {
                // 切换回可见模式 (abc123)
                textField.setVisible(true);
                textField.setManaged(true);
                passField.setVisible(false);
                passField.setManaged(false);
                // 改变按钮颜色表示当前是"可见"状态
                toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #007bff;");
            }
        });

        // 5. 调整输入框内边距，防止文字被右侧的按钮挡住
        passField.setPadding(new Insets(5, 30, 5, 5));
        textField.setPadding(new Insets(5, 30, 5, 5));

        // 6. 使用 StackPane 将它们堆叠在一起
        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(passField, textField, toggleBtn);
        // 将按钮对齐到右侧中间
        javafx.scene.layout.StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        javafx.scene.layout.StackPane.setMargin(toggleBtn, new Insets(0, 5, 0, 0)); // 右边距

        return pane;
    }
}