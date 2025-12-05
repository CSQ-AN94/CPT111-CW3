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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;

/**
 * MovieAppGUI - 最终修复版
 *
 * 修复说明：
 * 1. [Fix Error] 修复了 "Cannot resolve symbol 'mainLayout'" 报错。
 * - 原因：之前 mainLayout 漏掉了类成员变量的声明。
 * - 解决：在类开头添加了 private BorderPane mainLayout;
 * 2. 保持了之前所有功能（窗口大小固定、绿色注册按钮、列表操作按钮等）。
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
    private Scene registerScene;
    private Scene mainScene;

    // 统一窗口大小常量
    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 700;

    // --- 关键修复：添加 mainLayout 声明 ---
    // 这个变量需要在多个方法中访问（例如点击侧边栏切换视图），所以必须是类成员变量
    private BorderPane mainLayout;

    // 全局列表
    private ListView<Movie> globalMovieListView;
    private ListView<Movie> globalWatchlistView;
    private ListView<String> globalHistoryListView;
    private ListView<RecommendationEngine.MovieScore> globalRecListView;

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
        // 设置最小尺寸，防止用户拖太小
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);

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
    //              1. 登录界面
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

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(140);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(140);
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);

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

        // 修复点：强制指定场景大小，防止窗口塌缩
        return new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // ==========================================
    //              2. 注册界面
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

        Button submitBtn = new Button("Register & Login");
        submitBtn.setPrefWidth(300);
        submitBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-underline: true;");

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

                User newUser = new User(u, p);
                allUsers.add(newUser);
                userHandler.saveUsers(USERS_FILE, allUsers);

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

        // 修复点：强制指定场景大小，防止窗口塌缩
        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // ==========================================
    //              3. 主界面布局
    // ==========================================
    private Scene buildMainScene() {
        // 在这里初始化成员变量 mainLayout
        mainLayout = new BorderPane();

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        mainLayout.setCenter(createAllMoviesView());

        // 统一使用相同的大小
        return new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
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

        // 侧边栏按钮事件：切换 Center 区域
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
    //      View 1: All Movies
    // ==========================================
    private Node createAllMoviesView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        Label header = new Label("All Movies Repository");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        layout.setTop(header);

        Label statusLabel = new Label("Select actions from the list below.");
        statusLabel.setPadding(new Insets(5));
        layout.setBottom(statusLabel);

        globalMovieListView = new ListView<>();
        if (allMovies != null) {
            globalMovieListView.setItems(FXCollections.observableArrayList(allMovies));
        }

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
                            VBox infoBox = new VBox(5);

                            Label titleLbl = new Label("[" + movie.getId() + "] " + movie.getTitle());
                            titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                            Label detailLbl = new Label(String.format("Genre: %s | Year: %d | Rating: %.1f",
                                    movie.getGenre(), movie.getYear(), movie.getRating()));
                            detailLbl.setTextFill(Color.web("#555555"));
                            detailLbl.setFont(Font.font("Arial", 12));

                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button btnAdd = new Button("Add to Watchlist");
                            btnAdd.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");
                            btnAdd.setPrefWidth(120);

                            Button btnWatch = new Button("Mark as Watched");
                            btnWatch.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 11px;");
                            btnWatch.setPrefWidth(120);

                            btnAdd.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    if (currentUser.getWatchlist().contains(movie.getId())) {
                                        statusLabel.setText("Movie already in watchlist.");
                                        statusLabel.setTextFill(Color.ORANGE);
                                    } else {
                                        currentUser.addToWatchlist(movie.getId());
                                        userHandler.saveUsers(USERS_FILE, allUsers);
                                        statusLabel.setText("Added: " + movie.getTitle());
                                        statusLabel.setTextFill(Color.GREEN);
                                    }
                                }
                            });

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
    //      View 2: Watchlist
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
                            VBox infoBox = new VBox(5);

                            Label titleLbl = new Label("[" + movie.getId() + "] " + movie.getTitle());
                            titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                            Label detailLbl = new Label(String.format("Genre: %s | Year: %d | Rating: %.1f",
                                    movie.getGenre(), movie.getYear(), movie.getRating()));
                            detailLbl.setTextFill(Color.web("#555555"));
                            detailLbl.setFont(Font.font("Arial", 12));

                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button btnRemove = new Button("Remove");
                            btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px;");
                            btnRemove.setPrefWidth(120);

                            Button btnWatch = new Button("Mark as Watched");
                            btnWatch.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 11px;");
                            btnWatch.setPrefWidth(120);

                            btnRemove.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    currentUser.removeFromWatchlist(movie.getId());
                                    userHandler.saveUsers(USERS_FILE, allUsers);
                                    refreshWatchlistData();
                                    statusLabel.setText("Removed: " + movie.getTitle());
                                }
                            });

                            btnWatch.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    handleMarkWatchedLogic(movie, statusLabel);
                                    refreshWatchlistData();
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

        globalHistoryListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String[] parts = item.split("@");
                            VBox infoBox = new VBox(5);
                            Label titleLbl = new Label(item);
                            Label detailLbl = new Label("");

                            if (parts.length == 2) {
                                Movie m = movieHandler.findMovieById(allMovies, parts[0]);
                                String date = parts[1];
                                if (m != null) {
                                    titleLbl.setText("[" + m.getId() + "] " + m.getTitle());
                                    titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                                    detailLbl.setText(String.format("Genre: %s | Year: %d | Rating: %.1f | Date: %s",
                                            m.getGenre(), m.getYear(), m.getRating(), date));
                                    detailLbl.setTextFill(Color.web("#555555"));
                                    detailLbl.setFont(Font.font("Arial", 12));
                                }
                            }

                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button btnRemoveRow = new Button("Remove");
                            btnRemoveRow.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");

                            btnRemoveRow.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    currentUser.getHistory().remove(item);
                                    userHandler.saveUsers(USERS_FILE, allUsers);
                                    refreshHistoryData();
                                }
                            });

                            HBox row = new HBox(10);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.getChildren().addAll(infoBox, spacer, btnRemoveRow);
                            setGraphic(row);
                        }
                    }
                };
            }
        });

        layout.setCenter(globalHistoryListView);

        Button btnClear = new Button("Clear All History");
        btnClear.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");

        Label statusLabel = new Label("");
        statusLabel.setPadding(new Insets(0, 0, 0, 10));

        btnClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (currentUser.getHistory().isEmpty()) {
                    statusLabel.setText("History is already empty.");
                    statusLabel.setTextFill(Color.ORANGE);
                } else {
                    currentUser.getHistory().clear();
                    userHandler.saveUsers(USERS_FILE, allUsers);
                    refreshHistoryData();
                    statusLabel.setText("History cleared successfully.");
                    statusLabel.setTextFill(Color.GREEN);
                }
            }
        });

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.getChildren().addAll(btnClear, statusLabel);

        layout.setBottom(bottomBox);

        return layout;
    }

    // ==========================================
    //    View 4: Recommendations
    // ==========================================
    private Node createRecommendationsView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        VBox topControls = new VBox(10);
        topControls.setPadding(new Insets(0, 0, 15, 0));

        Label header = new Label("Get Recommendations");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);
        CheckBox cbGenre = new CheckBox("Genre (Type)");
        CheckBox cbYear = new CheckBox("Year (Era)");
        CheckBox cbRating = new CheckBox("Rating (Score)");
        cbGenre.setSelected(true); cbYear.setSelected(true); cbRating.setSelected(true);

        TextField numField = new TextField("5");
        numField.setPrefWidth(50);
        filters.getChildren().addAll(new Label("Criteria:"), cbGenre, cbYear, cbRating, new Label("Count:"), numField);

        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");

        Button addAllBtn = new Button("Add All to Watchlist");
        addAllBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addAllBtn.setDisable(true);

        actionButtons.getChildren().addAll(generateBtn, addAllBtn);

        topControls.getChildren().addAll(header, filters, actionButtons);
        mainLayout.setTop(topControls);

        globalRecListView = new ListView<>();
        mainLayout.setCenter(globalRecListView);

        Label statusLabel = new Label("Click Generate to get movie suggestions.");
        mainLayout.setBottom(statusLabel);

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

                    if (recs.isEmpty()) {
                        statusLabel.setText("No recommendations found based on your history.");
                        statusLabel.setTextFill(Color.ORANGE);
                        globalRecListView.setItems(FXCollections.observableArrayList());
                        addAllBtn.setDisable(true);
                    } else {
                        globalRecListView.setItems(FXCollections.observableArrayList(recs));
                        statusLabel.setText("Found " + recs.size() + " recommendations.");
                        statusLabel.setTextFill(Color.GREEN);
                        addAllBtn.setDisable(false);
                    }

                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid number input.");
                    statusLabel.setTextFill(Color.RED);
                }
            }
        });

        addAllBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<RecommendationEngine.MovieScore> items = globalRecListView.getItems();
                if (items == null || items.isEmpty()) return;

                int count = 0;
                for (RecommendationEngine.MovieScore ms : items) {
                    if (!currentUser.getWatchlist().contains(ms.movie.getId())) {
                        currentUser.addToWatchlist(ms.movie.getId());
                        count++;
                    }
                }
                userHandler.saveUsers(USERS_FILE, allUsers);
                statusLabel.setText("Added " + count + " new movies to watchlist.");
                globalRecListView.refresh();
            }
        });

        globalRecListView.setCellFactory(new Callback<ListView<RecommendationEngine.MovieScore>, ListCell<RecommendationEngine.MovieScore>>() {
            @Override
            public ListCell<RecommendationEngine.MovieScore> call(ListView<RecommendationEngine.MovieScore> param) {
                return new ListCell<RecommendationEngine.MovieScore>() {
                    @Override
                    protected void updateItem(RecommendationEngine.MovieScore item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox infoBox = new VBox(5);

                            Label titleLbl = new Label("[" + item.movie.getId() + "] " + item.movie.getTitle());
                            titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                            Label detailLbl = new Label(String.format("Score: %.2f | Genre: %s | Year: %d | Rating: %.1f",
                                    item.score * 10, item.movie.getGenre(), item.movie.getYear(), item.movie.getRating()));
                            detailLbl.setTextFill(Color.web("#555555"));
                            detailLbl.setFont(Font.font("Arial", 12));

                            infoBox.getChildren().addAll(titleLbl, detailLbl);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button btnAdd = new Button("Add to Watchlist");
                            btnAdd.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");

                            btnAdd.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    if (currentUser.getWatchlist().contains(item.movie.getId())) {
                                        statusLabel.setText("Already in watchlist: " + item.movie.getTitle());
                                        statusLabel.setTextFill(Color.ORANGE);
                                    } else {
                                        currentUser.addToWatchlist(item.movie.getId());
                                        userHandler.saveUsers(USERS_FILE, allUsers);
                                        statusLabel.setText("Added: " + item.movie.getTitle());
                                        statusLabel.setTextFill(Color.GREEN);
                                    }
                                }
                            });

                            HBox row = new HBox(10);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.getChildren().addAll(infoBox, spacer, btnAdd);
                            setGraphic(row);
                        }
                    }
                };
            }
        });

        return mainLayout;
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

        p1.setMaxWidth(300); p2.setMaxWidth(300); p3.setMaxWidth(300);

        Label msg = new Label();
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

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

    private void handleMarkWatchedLogic(Movie movie, Label statusLabel) {
        String today = java.time.LocalDate.now().toString();

        currentUser.addToHistory(movie.getId(), today);

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

        ArrayList<String> history = currentUser.getHistory();
        ArrayList<String> displayList = new ArrayList<>(history);
        Collections.reverse(displayList);

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