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
 * GUI 优化 (根据用户要求):
 * 1. 登录/注册界面使用 BorderPane/VBox 组合，确保内容居中且铺满窗口。
 * 2. 密码框的“小眼睛”功能优化，确保切换时大小不变，且按钮在框内右侧。
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

    // saved window size to preserve when switching scenes
    private double savedWidth = 900;
    private double savedHeight = 600;

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
        // 注意：这里先用默认尺寸构建，后面在 switchScene 时会保持舞台尺寸
        this.loginScene = buildLoginScene();
        this.registerScene = buildRegisterScene();
        this.mainScene = buildMainScene();

        // 3. 设置初始舞台
        primaryStage.setTitle("Movie Recommendation System");
        // 使用 switchScene 以便保存/恢复窗口大小逻辑生效
        switchScene(loginScene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);

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
     * 优化点: 使用 BorderPane 作为根布局，确保 VBox 居中且整个场景铺满窗口。
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

        Button goToRegisterButton = new Button("Register");
        goToRegisterButton.setPrefWidth(100);

        // 登录事件 (逻辑不变)
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
                switchScene(mainScene);
            }
        });

        // 跳转注册事件
        goToRegisterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                infoLabel.setText("");
                switchScene(registerScene);
                primaryStage.setTitle("Register New User");
            }
        });

        // **优化点 1: 确保内容居中且输入框宽度一致**
        // VBox 包含所有输入和按钮
        VBox centerContent = new VBox(15);

        // 这里使用变量接收返回的 StackPane，这样可以对 pane 设置 maxWidth，而不会与内部绑定冲突
        StackPane passPane = createPasswordWithEye(passField);

        // 设置一致的宽度（外部可设置）
        final double MAX_INPUT_WIDTH = 300;
        userField.setMaxWidth(MAX_INPUT_WIDTH);
        passPane.setMaxWidth(MAX_INPUT_WIDTH);

        centerContent.getChildren().addAll(
                titleLabel,
                userLabel,
                userField,
                passLabel,
                passPane, // <--- 调用优化的方法
                loginButton,
                goToRegisterButton,
                infoLabel
        );
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50));

        // **优化点 2: 使用 BorderPane 铺满整个场景**
        BorderPane root = new BorderPane();
        root.setCenter(centerContent);
        // 整个 BorderPane 会自动适应 Scene 的大小

        // 场景现在会铺满整个舞台
        return new Scene(root);
    }

    /**
     * === 场景 2: 注册界面 ===
     * 优化点: 使用 BorderPane 作为根布局，确保内容居中且整个场景铺满窗口。
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
        userFld.setPromptText("Enter username");

        Label passLbl = new Label("Password:");
        PasswordField passFld = new PasswordField();

        Label confirmLbl = new Label("Confirm Password:");
        PasswordField confirmFld = new PasswordField();

        // 使用 createPasswordWithEye 并设置 pane 的最大宽度
        StackPane passPane = createPasswordWithEye(passFld);
        StackPane confirmPane = createPasswordWithEye(confirmFld);

        final double MAX_INPUT_WIDTH = 300;
        userFld.setMaxWidth(MAX_INPUT_WIDTH);
        passPane.setMaxWidth(MAX_INPUT_WIDTH);
        confirmPane.setMaxWidth(MAX_INPUT_WIDTH);

        grid.add(userLbl, 0, 0);
        grid.add(userFld, 1, 0);
        grid.add(passLbl, 0, 1);
        grid.add(passPane, 1, 1); // <--- 使用优化的密码框
        grid.add(confirmLbl, 0, 2);
        grid.add(confirmPane, 1, 2); // <--- 确认密码也使用优化的密码框

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: red;");

        Button registerBtn = new Button("Register & Login");
        Button backBtn = new Button("Back to Login");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerBtn, backBtn);

        // 注册事件 (逻辑不变)
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
                switchScene(mainScene);
                showMessage("Welcome, " + uName + "! Registration successful.");
            }
        });

        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                userFld.clear(); passFld.clear(); confirmFld.clear(); errorMsg.setText("");
                switchScene(loginScene);
                primaryStage.setTitle("Movie Recommendation System");
            }
        });

        // **优化点 1: 确保内容居中**
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(headerLabel, grid, errorMsg, buttonBox);

        // **优化点 2: 使用 BorderPane 铺满整个场景**
        BorderPane root = new BorderPane();
        root.setCenter(centerContent);

        return new Scene(root);
    }

    /**
     * === 场景 3: 主功能界面 === (无修改)
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

        // 使用 java.time.LocalDate 获取当前日期
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
     * 处理推荐功能 - 弹出自定义设置窗口
     */
    private void handleRecommendations() {
        if (currentUser == null) return;

        final Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Recommendation Settings");

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label("How many movies?");
        TextField numField = new TextField("5");
        numField.setMaxWidth(100);

        Label criteriaLabel = new Label("Select Criteria to Analyze:");
        criteriaLabel.setStyle("-fx-font-weight: bold;");

        CheckBox cbGenre = new CheckBox("Genre (Based on what types you watch)");
        CheckBox cbYear = new CheckBox("Year (Based on your preferred era)");
        CheckBox cbRating = new CheckBox("Rating (Based on high quality)");

        cbGenre.setSelected(true);
        cbYear.setSelected(true);
        cbRating.setSelected(true);

        Button btnConfirm = new Button("Get Recommendations");

        btnConfirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    int n = Integer.parseInt(numField.getText().trim());
                    if (n <= 0) {
                        showMessage("Please enter a positive number.");
                        return;
                    }

                    boolean useGenre = cbGenre.isSelected();
                    boolean useYear = cbYear.isSelected();
                    boolean useRating = cbRating.isSelected();

                    if (!useGenre && !useYear && !useRating) {
                        showMessage("No criteria selected. Defaulting to ALL.");
                        useGenre = true; useYear = true; useRating = true;
                    }

                    RecommendationEngine engine = new RecommendationEngine(allMovies);
                    ArrayList<RecommendationEngine.MovieScore> recs =
                            engine.getRecommendations(currentUser, n, useGenre, useYear, useRating);

                    showMessage("\n=== Recommendations (G=" + useGenre + ", Y=" + useYear + ", R=" + useRating + ") ===");
                    if (recs.isEmpty()) {
                        showMessage("No recommendations available.");
                    } else {
                        for (RecommendationEngine.MovieScore ms : recs) {
                            showMessage(String.format("Score: %.1f | %s", ms.score * 10, ms.movie.toString()));
                        }
                    }

                    dialogStage.close();

                } catch (NumberFormatException ex) {
                    showMessage("Invalid number input.");
                }
            }
        });

        layout.getChildren().addAll(numLabel, numField, new Separator(), criteriaLabel, cbGenre, cbYear, cbRating, new Separator(), btnConfirm);

        Scene dialogScene = new Scene(layout, 320, 360);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    /**
     * Change Password: 使用与登录/注册一致的 createPasswordWithEye
     */
    private void handleChangePassword() {
        if (currentUser == null) return;
        Stage pwdStage = new Stage();
        pwdStage.initModality(Modality.APPLICATION_MODAL);
        pwdStage.setTitle("Change Password");

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER_LEFT);

        PasswordField oldF = new PasswordField();
        oldF.setPromptText("Old Password");
        StackPane oldPane = createPasswordWithEye(oldF);

        PasswordField newF = new PasswordField();
        newF.setPromptText("New Password");
        StackPane newPane = createPasswordWithEye(newF);

        PasswordField conF = new PasswordField();
        conF.setPromptText("Confirm New Password");
        StackPane conPane = createPasswordWithEye(conF);

        final double MAX_INPUT_WIDTH = 300;
        oldPane.setMaxWidth(MAX_INPUT_WIDTH);
        newPane.setMaxWidth(MAX_INPUT_WIDTH);
        conPane.setMaxWidth(MAX_INPUT_WIDTH);

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

        box.getChildren().addAll(new Label("Change Password"), oldPane, newPane, conPane, errL, saveBtn);
        Scene s = new Scene(box, 380, 320);
        pwdStage.setScene(s);
        pwdStage.show();
    }

    private void handleLogout() {
        userHandler.saveUsers(USERS_FILE, allUsers);
        currentUser = null;
        if (messageArea != null) messageArea.clear();
        primaryStage.setTitle("Movie Recommendation System");
        switchScene(loginScene);
    }

    /**
     * 切换场景时保持窗口大小（保存并恢复舞台宽高），避免 Scene 切换导致窗口缩小。
     */
    private void switchScene(Scene newScene) {
        if (primaryStage != null) {
            // 保存当前舞台宽高（若舞台尚未显示则使用已有 savedWidth/savedHeight）
            try {
                double w = primaryStage.getWidth();
                double h = primaryStage.getHeight();
                if (w > 0 && h > 0) {
                    savedWidth = w;
                    savedHeight = h;
                }
            } catch (Exception ex) {
                // ignore
            }
        }

        // 切换场景
        if (primaryStage != null) {
            primaryStage.setScene(newScene);

            // 恢复之前大小（防止新 Scene 的首选大小导致窗口缩小）
            try {
                if (savedWidth > 0) primaryStage.setWidth(savedWidth);
                if (savedHeight > 0) primaryStage.setHeight(savedHeight);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    /**
     * 创建带"小眼睛"切换可见性的密码输入框组件
     * @param passField 已经创建好的 PasswordField 对象（用于保存密码值）
     * @return 包含输入框和切换按钮的 StackPane
     */
    private javafx.scene.layout.StackPane createPasswordWithEye(PasswordField passField) {
        // 1. 创建一个用于显示明文的 TextField
        TextField textField = new TextField();
        // 让两个框的内容始终同步
        textField.textProperty().bindBidirectional(passField.textProperty());
        textField.setPromptText(passField.getPromptText());

        // 2. 创建"小眼睛"按钮
        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px;");
        toggleBtn.setFocusTraversable(false);

        // 3. 初始设置：默认显示密码框 (passField)，隐藏明文框 (textField)
        textField.setOpacity(0.0);
        textField.setDisable(true); // 禁用输入
        passField.setOpacity(1.0);
        passField.setDisable(false); // 启用输入

        // 4. 按钮点击事件：切换显示/隐藏
        toggleBtn.setOnAction(e -> {
            // 检查当前是否显示的是密码框 (passField)
            boolean isPasswordVisible = passField.getOpacity() == 1.0;

            if (isPasswordVisible) {
                // 切换到可见模式 (明文)
                textField.setOpacity(1.0);
                textField.setDisable(false);
                passField.setOpacity(0.0);
                passField.setDisable(true);
                // 改变按钮颜色表示当前是"可见"状态
                toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: #007bff;");
            } else {
                // 切换回隐藏模式 (••••)
                textField.setOpacity(0.0);
                textField.setDisable(true);
                passField.setOpacity(1.0);
                passField.setDisable(false);
                toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-text-fill: black;");
            }
            // 强制聚焦到当前激活的输入框
            if (!textField.isDisable()) {
                textField.requestFocus();
            } else {
                passField.requestFocus();
            }
        });

        // 5. 调整输入框内边距，防止文字被右侧的按钮挡住
        Insets inputInsets = new Insets(6, 30, 6, 8); // 右侧留出空间给按钮
        passField.setPadding(inputInsets);
        textField.setPadding(inputInsets);

        // 确保两个框的宽度和高度属性一致（绑定）
        passField.maxWidthProperty().bind(textField.maxWidthProperty());
        passField.minWidthProperty().bind(textField.minWidthProperty());
        passField.prefWidthProperty().bind(textField.prefWidthProperty());

        // 6. 使用 StackPane 将它们堆叠在一起
        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(passField, textField, toggleBtn);

        // 将按钮对齐到右侧中间
        javafx.scene.layout.StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        javafx.scene.layout.StackPane.setMargin(toggleBtn, new Insets(0, 6, 0, 0)); // 右边距

        // NOTE: 不再将 pane.maxWidthProperty 绑定到 passField（这样外部可以安全调用 pane.setMaxWidth(...)）
        return pane;
    }
}