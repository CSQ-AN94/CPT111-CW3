import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;

/**
 * MovieAppGUI - 最终增强版
 *
 * 修改亮点：
 * 1. 登录界面：Login 和 Register 按钮并排，Register 为绿色。
 * 2. 列表视图：使用 setCellFactory 自定义列表行，在每一行电影后面直接添加操作按钮。
 * - All Movies: [Add to Watchlist] [Mark as Watched]
 * - Watchlist:  [Remove] [Mark as Watched]
 * 3. 推荐系统：分数显示保留两位小数 (%.2f)。
 * 4. 逻辑：严格遵守匿名内部类写法，支持二刷。
 */
public class MovieAppGUI extends Application {

    // --- 文件路径 ---
    private static final String MOVIES_FILE = "data/movies.csv";
    private static final String USERS_FILE = "data/users.csv";

    // --- 数据对象 ---
    private ArrayList<Movie> allMovies;
    private ArrayList<User> allUsers;
    private User currentUser;

    // --- 工具类 ---
    private MovieFileHandler movieHandler = new MovieFileHandler();
    private UserFileHandler userHandler = new UserFileHandler();

    // --- UI 组件 ---
    private Stage primaryStage;
    private Scene loginScene;
    private Scene registerScene; // 新增注册场景引用
    private Scene mainScene;

    // 主布局容器
    private BorderPane mainLayout;

    // 全局列表，方便刷新
    private ListView<Movie> globalMovieListView;
    private ListView<Movie> globalWatchlistView;
    private ListView<String> globalHistoryListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. 加载数据
        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        // 2. 初始化场景
        this.loginScene = buildLoginScene();
        this.registerScene = buildRegisterScene();

        // 3. 设置舞台
        primaryStage.setTitle("Movie Recommendation System");
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(1000); //稍微加宽一点以便显示按钮
        primaryStage.setMinHeight(650);

        // 4. 关闭保存
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

    // ==========================================
    //              1. 登录界面 (Login View)
    // ==========================================
    private Scene buildLoginScene() {
        Label titleLabel = new Label("Movie Recommendation System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setMaxWidth(300);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        StackPane passPane = createPasswordWithEye(passField);
        passPane.setMaxWidth(300);

        Label infoLabel = new Label();
        infoLabel.setTextFill(Color.RED);

        // --- 按钮区域 ---
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(140);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(140);
        // 设置为绿色
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        // 按钮并排布局
        HBox buttonBox = new HBox(20); // 间距20
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        // 登录逻辑
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String u = userField.getText().trim();
                String p = passField.getText().trim();

                if (u.isEmpty() || p.isEmpty()) {
                    infoLabel.setText("Please enter username and password.");
                    return;
                }

                User user = userHandler.findUserByUsername(allUsers, u);
                if (user != null && user.verifyPassword(p)) {
                    currentUser = user;
                    userField.clear();
                    passField.clear();
                    infoLabel.setText("");

                    mainScene = buildMainScene();
                    primaryStage.setTitle("Welcome, " + currentUser.getUsername());
                    primaryStage.setScene(mainScene);
                } else {
                    infoLabel.setText("Invalid username or password.");
                }
            }
        });

        // 注册跳转逻辑
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                userField.clear();
                passField.clear();
                infoLabel.setText("");
                primaryStage.setScene(registerScene);
                primaryStage.setTitle("Register New User");
            }
        });

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, userField, passPane, buttonBox, infoLabel);

        return new Scene(layout);
    }

    // ==========================================
    //              2. 注册界面 (Register View)
    // ==========================================
    private Scene buildRegisterScene() {
        Label headerLabel = new Label("Create New Account");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox form = new VBox(15);
        form.setMaxWidth(300);
        form.setAlignment(Pos.CENTER);

        TextField userFld = new TextField();
        userFld.setPromptText("Enter username");

        PasswordField passFld = new PasswordField();
        passFld.setPromptText("Enter password");
        StackPane passPane = createPasswordWithEye(passFld);

        PasswordField confirmFld = new PasswordField();
        confirmFld.setPromptText("Confirm password");
        StackPane confirmPane = createPasswordWithEye(confirmFld);

        Label errorMsg = new Label();
        errorMsg.setStyle("-fx-text-fill: red;");

        Button submitBtn = new Button("Register Now");
        submitBtn.setPrefWidth(300);
        submitBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-underline: true;");

        // 注册逻辑
        submitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String u = userFld.getText().trim();
                String p = passFld.getText().trim();
                String c = confirmFld.getText().trim();

                if (u.isEmpty() || p.isEmpty()) {
                    errorMsg.setText("All fields are required.");
                    return;
                }
                if (!p.equals(c)) {
                    errorMsg.setText("Passwords do not match.");
                    return;
                }
                if (userHandler.findUserByUsername(allUsers, u) != null) {
                    errorMsg.setText("Username already taken.");
                    return;
                }

                // 创建新用户
                User newUser = new User(u, p);
                allUsers.add(newUser);
                userHandler.saveUsers(USERS_FILE, allUsers);

                // 注册成功直接登录
                currentUser = newUser;
                userFld.clear(); passFld.clear(); confirmFld.clear(); errorMsg.setText("");

                mainScene = buildMainScene();
                primaryStage.setTitle("Welcome, " + currentUser.getUsername());
                primaryStage.setScene(mainScene);
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

        form.getChildren().addAll(headerLabel, userFld, passPane, confirmPane, errorMsg, submitBtn, backBtn);

        StackPane root = new StackPane(form);
        return new Scene(root);
    }

    // ==========================================
    //              3. 主界面 (Main View)
    // ==========================================
    private Scene buildMainScene() {
        mainLayout = new BorderPane();

        // 左侧导航栏
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // 默认显示 All Movies
        mainLayout.setCenter(createAllMoviesView());

        return new Scene(mainLayout, 1100, 650);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #2c3e50;");

        Label welcomeLabel = new Label("Welcome,\n" + currentUser.getUsername());
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        welcomeLabel.setPadding(new Insets(0, 0, 20, 10));

        Button btnChangePass = createSidebarButton("Change Password");
        Button btnAllMovies = createSidebarButton("All Movies");
        Button btnWatchlist = createSidebarButton("Watchlist");
        Button btnHistory = createSidebarButton("History");
        Button btnRecs = createSidebarButton("Recommendations");
        Button btnLogout = createSidebarButton("Logout");

        btnChangePass.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { mainLayout.setCenter(createChangePasswordView()); } });
        btnAllMovies.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { mainLayout.setCenter(createAllMoviesView()); } });
        btnWatchlist.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { mainLayout.setCenter(createWatchlistView()); } });
        btnHistory.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { mainLayout.setCenter(createHistoryView()); } });
        btnRecs.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { mainLayout.setCenter(createRecommendationsView()); } });
        btnLogout.setOnAction(new EventHandler<ActionEvent>() { @Override public void handle(ActionEvent e) { handleLogout(); } });

        btnLogout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-radius: 5;");

        sidebar.getChildren().addAll(welcomeLabel, btnChangePass, btnAllMovies, btnWatchlist, btnHistory, btnRecs, new Separator(), btnLogout);
        return sidebar;
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setFont(Font.font("Arial", 14));

        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;";

        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(new EventHandler<javafx.scene.input.MouseEvent>() { @Override public void handle(javafx.scene.input.MouseEvent e) { if(!btn.getText().equals("Logout")) btn.setStyle(hoverStyle); } });
        btn.setOnMouseExited(new EventHandler<javafx.scene.input.MouseEvent>() { @Override public void handle(javafx.scene.input.MouseEvent e) { if(!btn.getText().equals("Logout")) btn.setStyle(normalStyle); } });

        return btn;
    }

    // ==========================================
    //         View 1: All Movies (带行内按钮)
    // ==========================================
    private Node createAllMoviesView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label header = new Label("All Movies Repository");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        layout.setTop(header);

        // 状态栏
        Label statusLabel = new Label("Select actions from the list below.");
        statusLabel.setPadding(new Insets(5));
        layout.setBottom(statusLabel);

        globalMovieListView = new ListView<>();
        if (allMovies != null) {
            globalMovieListView.setItems(FXCollections.observableArrayList(allMovies));
        }

        // --- 核心修改：自定义 Cell Factory ---
        globalMovieListView.setCellFactory(new Callback<ListView<Movie>, ListCell<Movie>>() {
            @Override
            public ListCell<Movie> call(ListView<Movie> param) {
                return new ListCell<Movie>() {
                    @Override
                    protected void updateItem(Movie movie, boolean empty) {
                        super.updateItem(movie, empty);

                        if (empty || movie == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // 左侧：电影信息
                            VBox infoBox = new VBox(2);
                            Label titleLbl = new Label(movie.getTitle());
                            titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                            Label detailLbl = new Label(String.format("%s | %d | %.1f", movie.getGenre(), movie.getYear(), movie.getRating()));
                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            // 中间：占位符（把按钮挤到右边）
                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            // 右侧：按钮组
                            Button btnAdd = new Button("Add to Watchlist");
                            btnAdd.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");

                            Button btnWatch = new Button("Mark as Watched");
                            btnWatch.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 11px;");

                            // 按钮逻辑：Add
                            btnAdd.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    if (currentUser.getWatchlist().contains(movie.getId())) {
                                        statusLabel.setText(movie.getTitle() + " is already in watchlist.");
                                        statusLabel.setTextFill(Color.ORANGE);
                                    } else {
                                        // 逻辑修改：允许二刷，不检查 History
                                        currentUser.addToWatchlist(movie.getId());
                                        userHandler.saveUsers(USERS_FILE, allUsers);
                                        statusLabel.setText("Added to watchlist: " + movie.getTitle());
                                        statusLabel.setTextFill(Color.GREEN);
                                    }
                                }
                            });

                            // 按钮逻辑：Watch
                            btnWatch.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    handleMarkWatchedLogic(movie, statusLabel);
                                }
                            });

                            HBox row = new HBox(10);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.getChildren().addAll(infoBox, spacer, btnAdd, btnWatch);

                            setGraphic(row);
                        }
                    }
                };
            }
        });

        layout.setCenter(globalMovieListView);
        return layout;
    }

    // ==========================================
    //         View 2: Watchlist (带行内按钮)
    // ==========================================
    private Node createWatchlistView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label header = new Label("My Watchlist");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        layout.setTop(header);

        Label statusLabel = new Label("Manage your watchlist.");
        statusLabel.setPadding(new Insets(5));
        layout.setBottom(statusLabel);

        globalWatchlistView = new ListView<>();
        refreshWatchlistData();

        // --- 核心修改：自定义 Cell Factory ---
        globalWatchlistView.setCellFactory(new Callback<ListView<Movie>, ListCell<Movie>>() {
            @Override
            public ListCell<Movie> call(ListView<Movie> param) {
                return new ListCell<Movie>() {
                    @Override
                    protected void updateItem(Movie movie, boolean empty) {
                        super.updateItem(movie, empty);

                        if (empty || movie == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox infoBox = new VBox(2);
                            Label titleLbl = new Label(movie.getTitle());
                            titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                            Label detailLbl = new Label(movie.getGenre() + " (" + movie.getYear() + ")");
                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            // 按钮：Remove (红色)
                            Button btnRemove = new Button("Remove");
                            btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");

                            // 按钮：Watch (橙色)
                            Button btnWatch = new Button("Mark as Watched");
                            btnWatch.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 11px;");

                            // 逻辑：Remove
                            btnRemove.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    currentUser.removeFromWatchlist(movie.getId());
                                    userHandler.saveUsers(USERS_FILE, allUsers);
                                    refreshWatchlistData(); // 刷新列表
                                    statusLabel.setText("Removed: " + movie.getTitle());
                                }
                            });

                            // 逻辑：Watch
                            btnWatch.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    handleMarkWatchedLogic(movie, statusLabel);
                                    refreshWatchlistData(); // 列表会刷新
                                }
                            });

                            HBox row = new HBox(10);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.getChildren().addAll(infoBox, spacer, btnRemove, btnWatch);
                            setGraphic(row);
                        }
                    }
                };
            }
        });

        layout.setCenter(globalWatchlistView);
        return layout;
    }

    // ==========================================
    //         View 3: History
    // ==========================================
    private Node createHistoryView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label header = new Label("Watch History");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        layout.setTop(header);

        globalHistoryListView = new ListView<>();
        refreshHistoryData();
        layout.setCenter(globalHistoryListView);

        return layout;
    }

    // ==========================================
    //         View 4: Recommendations
    // ==========================================
    private Node createRecommendationsView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Get Recommendations");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        CheckBox cbGenre = new CheckBox("Genre");
        CheckBox cbYear = new CheckBox("Year");
        CheckBox cbRating = new CheckBox("Rating");
        cbGenre.setSelected(true); cbYear.setSelected(true); cbRating.setSelected(true);

        HBox numBox = new HBox(10);
        numBox.setAlignment(Pos.CENTER_LEFT);
        TextField numField = new TextField("5");
        numField.setPrefWidth(50);
        numBox.getChildren().addAll(new Label("Count:"), numField);

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(300);

        generateBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    int n = Integer.parseInt(numField.getText().trim());
                    if (n <= 0) throw new NumberFormatException();

                    RecommendationEngine engine = new RecommendationEngine(allMovies);
                    ArrayList<RecommendationEngine.MovieScore> recs = engine.getRecommendations(
                            currentUser, n, cbGenre.isSelected(), cbYear.isSelected(), cbRating.isSelected()
                    );

                    StringBuilder sb = new StringBuilder();
                    sb.append("Top Recommendations:\n\n");
                    if (recs.isEmpty()) {
                        sb.append("No matches found.");
                    } else {
                        for (RecommendationEngine.MovieScore ms : recs) {
                            // --- 修改：保留两位小数 ---
                            sb.append(String.format("[Score: %.2f] %s\n", ms.score * 10, ms.movie.toString()));
                        }
                    }
                    resultArea.setText(sb.toString());

                } catch (NumberFormatException ex) {
                    resultArea.setText("Invalid number.");
                }
            }
        });

        layout.getChildren().addAll(header, new Label("Criteria:"), cbGenre, cbYear, cbRating, numBox, generateBtn, resultArea);
        return layout;
    }

    // ==========================================
    //         View 5: Change Password
    // ==========================================
    private Node createChangePasswordView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        Label header = new Label("Change Password");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        PasswordField oldF = new PasswordField(); oldF.setPromptText("Old Password");
        PasswordField newF = new PasswordField(); newF.setPromptText("New Password");
        PasswordField conF = new PasswordField(); conF.setPromptText("Confirm Password");

        StackPane p1 = createPasswordWithEye(oldF);
        StackPane p2 = createPasswordWithEye(newF);
        StackPane p3 = createPasswordWithEye(conF);

        // 限制宽度
        p1.setMaxWidth(300); p2.setMaxWidth(300); p3.setMaxWidth(300);

        Label msg = new Label();
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!currentUser.verifyPassword(oldF.getText())) {
                    msg.setText("Incorrect old password.");
                    msg.setTextFill(Color.RED);
                } else if (newF.getText().isEmpty()) {
                    msg.setText("Password cannot be empty.");
                    msg.setTextFill(Color.RED);
                } else if (!newF.getText().equals(conF.getText())) {
                    msg.setText("New passwords do not match.");
                    msg.setTextFill(Color.RED);
                } else {
                    currentUser.setPassword(newF.getText());
                    userHandler.saveUsers(USERS_FILE, allUsers);
                    msg.setText("Success!");
                    msg.setTextFill(Color.GREEN);
                    oldF.clear(); newF.clear(); conF.clear();
                }
            }
        });

        layout.getChildren().addAll(header, new Label("Current:"), p1, new Label("New:"), p2, new Label("Confirm:"), p3, saveBtn, msg);
        return layout;
    }

    // ==========================================
    //              公共逻辑与工具
    // ==========================================

    private void handleLogout() {
        userHandler.saveUsers(USERS_FILE, allUsers);
        currentUser = null;
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Movie Recommendation System");
    }

    /**
     * 通用的"标记已看"逻辑
     */
    private void handleMarkWatchedLogic(Movie movie, Label statusLabel) {
        String today = java.time.LocalDate.now().toString();

        // 1. 添加到历史 (User类里已处理重复日期逻辑)
        currentUser.addToHistory(movie.getId(), today);

        // 2. 如果在待看列表，则移除
        if (currentUser.getWatchlist().contains(movie.getId())) {
            currentUser.removeFromWatchlist(movie.getId());
        }

        userHandler.saveUsers(USERS_FILE, allUsers);
        statusLabel.setText("Watched: " + movie.getTitle());
        statusLabel.setTextFill(Color.GREEN);
    }

    private void refreshWatchlistData() {
        if (globalWatchlistView == null || currentUser == null) return;
        ArrayList<Movie> list = new ArrayList<>();
        for (String id : currentUser.getWatchlist()) {
            Movie m = movieHandler.findMovieById(allMovies, id);
            if (m != null) list.add(m);
        }
        globalWatchlistView.setItems(FXCollections.observableArrayList(list));
    }

    private void refreshHistoryData() {
        if (globalHistoryListView == null || currentUser == null) return;
        ArrayList<String> displayList = new ArrayList<>();
        ArrayList<String> history = currentUser.getHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            String entry = history.get(i);
            String[] parts = entry.split("@");
            if (parts.length == 2) {
                Movie m = movieHandler.findMovieById(allMovies, parts[0]);
                if (m != null) displayList.add(String.format("%s (Date: %s)", m.getTitle(), parts[1]));
            }
        }
        globalHistoryListView.setItems(FXCollections.observableArrayList(displayList));
    }

    private StackPane createPasswordWithEye(PasswordField passField) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(passField.textProperty());
        textField.setPromptText(passField.getPromptText());

        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        toggleBtn.setFocusTraversable(false);

        textField.setVisible(false);

        toggleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (textField.isVisible()) {
                    textField.setVisible(false);
                    passField.setVisible(true);
                    toggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
                } else {
                    textField.setVisible(true);
                    passField.setVisible(false);
                    toggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db;");
                }
            }
        });

        StackPane pane = new StackPane(textField, passField, toggleBtn);
        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleBtn, new Insets(0, 5, 0, 0));
        return pane;
    }
}