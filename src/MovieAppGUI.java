import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.*;

/**
 * MovieAppGUI - Final Polish
 * 包括：History 和 Recommendations 中完整的电影细节 (Year, Rating)；
 * Recommendations 列表中的电影标题加粗。
 * 严格遵守 import.docx 约束。
 */
public class MovieAppGUI extends Application {

    // --- File Paths ---
    private static final String MOVIES_FILE = "data/movies.csv";
    private static final String USERS_FILE = "data/users.csv";

    // --- Data Objects ---
    private ArrayList<Movie> allMovies;
    private ArrayList<User> allUsers;
    private User currentUser;

    // --- Helpers ---
    private final MovieFileHandler movieHandler = new MovieFileHandler();
    private final UserFileHandler userHandler = new UserFileHandler();

    // --- UI Components ---
    private Stage primaryStage;
    private Scene loginScene;
    private Scene registerScene;
    private BorderPane mainLayout;

    // --- UI Lists ---
    private ListView<Movie> globalMovieListView;
    private ListView<Movie> globalWatchlistView;
    private ListView<String> globalHistoryListView;
    private ListView<RecommendationEngine.MovieScore> globalRecListView;

    // --- Constants ---
    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 700;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 1. Load Data
        allMovies = movieHandler.loadMovies(MOVIES_FILE);
        allUsers = userHandler.loadUsers(USERS_FILE);

        // 2. Initialize Scenes
        this.loginScene = buildLoginScene();
        this.registerScene = buildRegisterScene();

        // 3. Setup Stage
        primaryStage.setTitle("Movie Recommendation System");
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);

        // 4. Save on Close
        primaryStage.setOnCloseRequest(e -> {
            if (allUsers != null) {
                userHandler.saveUsers(USERS_FILE, allUsers);
            }
        });

        primaryStage.show();
    }

    // ==========================================
    //              SCENE 1: LOGIN
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

        Button loginButton = createStyledButton("Login", "#3498db");
        loginButton.setDefaultButton(true);

        Button registerButton = createStyledButton("Register", "#2ecc71");

        HBox buttonBox = new HBox(20);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Event Handling
        loginButton.setOnAction(e -> {
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
                enterMainApp();
            } else {
                infoLabel.setText("Invalid username or password.");
            }
        });

        registerButton.setOnAction(e -> {
            userField.clear();
            passField.clear();
            infoLabel.setText("");
            primaryStage.setScene(registerScene);
            primaryStage.setTitle("Register New User");
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(titleLabel, userField, passPane, buttonBox, infoLabel);
        layout.setAlignment(Pos.CENTER);
        return new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // ==========================================
    //              SCENE 2: REGISTER
    // ==========================================
    private Scene buildRegisterScene() {
        Label headerLabel = new Label("Create New Account");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

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

        Button submitBtn = createStyledButton("Register & Login", "#2ecc71");
        submitBtn.setPrefWidth(300);

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-underline: true;");

        submitBtn.setOnAction(e -> {
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
            enterMainApp();
        });

        backBtn.setOnAction(e -> {
            userFld.clear(); passFld.clear(); confirmFld.clear(); errorMsg.setText("");
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Movie Recommendation System");
        });

        VBox form = new VBox(15);
        form.getChildren().addAll(headerLabel, userFld, passPane, confirmPane, errorMsg, submitBtn, backBtn);
        form.setMaxWidth(300);
        form.setAlignment(Pos.CENTER);

        return new Scene(new StackPane(form), WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    // ==========================================
    //              SCENE 3: MAIN APP
    // ==========================================
    private void enterMainApp() {
        mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setCenter(createAllMoviesView()); // Default view

        Scene mainScene = new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Welcome, " + currentUser.getUsername());
        primaryStage.setScene(mainScene);
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

        // Navigation Buttons
        Button btnAllMovies = createSidebarButton("All Movies");
        Button btnWatchlist = createSidebarButton("Watchlist");
        Button btnHistory = createSidebarButton("History");
        Button btnRecs = createSidebarButton("Recommendations");
        Button btnChangePass = createSidebarButton("Change Password");
        Button btnLogout = createSidebarButton("Logout");

        // Set Actions
        btnAllMovies.setOnAction(e -> mainLayout.setCenter(createAllMoviesView()));
        btnWatchlist.setOnAction(e -> mainLayout.setCenter(createWatchlistView()));
        btnHistory.setOnAction(e -> mainLayout.setCenter(createHistoryView()));
        btnRecs.setOnAction(e -> mainLayout.setCenter(createRecommendationsView()));
        btnChangePass.setOnAction(e -> mainLayout.setCenter(createChangePasswordView()));
        btnLogout.setOnAction(e -> handleLogout());

        // Logout styling
        btnLogout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-radius: 5;");

        sidebar.getChildren().addAll(welcomeLabel, btnAllMovies, btnWatchlist, btnHistory, btnRecs, btnChangePass, new Separator(), btnLogout);
        return sidebar;
    }

    // --- VIEW: All Movies ---
    private Pane createAllMoviesView() {
        BorderPane layout = createBaseLayout("All Movies Repository", "Select actions from the list below.");
        Label statusLabel = (Label) layout.getBottom();

        globalMovieListView = new ListView<>();
        if (allMovies != null) {
            globalMovieListView.setItems(FXCollections.observableArrayList(allMovies));
        }

        globalMovieListView.setCellFactory(param -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Button btnAdd = createSmallButton("Add to Watchlist", "#3498db");
                    Button btnWatch = createSmallButton("Mark as Watched", "#e67e22");

                    btnAdd.setOnAction(e -> {
                        if (currentUser.getWatchlist().contains(movie.getId())) {
                            updateStatus(statusLabel, "Movie already in watchlist.", Color.ORANGE);
                        } else {
                            currentUser.addToWatchlist(movie.getId());
                            userHandler.saveUsers(USERS_FILE, allUsers);
                            updateStatus(statusLabel, "Added: " + movie.getTitle(), Color.GREEN);
                        }
                    });

                    btnWatch.setOnAction(e -> handleMarkWatchedLogic(movie, statusLabel));

                    setGraphic(createMovieRow(movie, btnAdd, btnWatch));
                }
            }
        });

        layout.setCenter(globalMovieListView);
        return layout;
    }

    // --- VIEW: Watchlist ---
    private Pane createWatchlistView() {
        BorderPane layout = createBaseLayout("My Watchlist", "Manage your watchlist.");
        Label statusLabel = (Label) layout.getBottom();

        globalWatchlistView = new ListView<>();
        refreshWatchlistData();

        globalWatchlistView.setCellFactory(param -> new ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Button btnRemove = createSmallButton("Remove", "#e74c3c");
                    Button btnWatch = createSmallButton("Mark as Watched", "#e67e22");

                    btnRemove.setOnAction(e -> {
                        currentUser.removeFromWatchlist(movie.getId());
                        userHandler.saveUsers(USERS_FILE, allUsers);
                        refreshWatchlistData();
                        updateStatus(statusLabel, "Removed: " + movie.getTitle(), Color.BLACK);
                    });

                    btnWatch.setOnAction(e -> {
                        handleMarkWatchedLogic(movie, statusLabel);
                        refreshWatchlistData();
                    });

                    setGraphic(createMovieRow(movie, btnRemove, btnWatch));
                }
            }
        });

        layout.setCenter(globalWatchlistView);
        return layout;
    }

    // --- VIEW: History (Updated) ---
    private Pane createHistoryView() {
        BorderPane layout = createBaseLayout("Watch History", "");

        globalHistoryListView = new ListView<>();
        refreshHistoryData();

        globalHistoryListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String[] parts = item.split("@");
                    VBox infoBox = new VBox(5);
                    if (parts.length == 2) {
                        Movie m = movieHandler.findMovieById(allMovies, parts[0]);
                        String date = parts[1];
                        if (m != null) {
                            Label title = new Label("[" + m.getId() + "] " + m.getTitle());
                            title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                            // [Updated]: Added Year and Rating
                            Label details = new Label(String.format("Genre: %s | Year: %d | Rating: %.1f | Date: %s",
                                    m.getGenre(), m.getYear(), m.getRating(), date));
                            details.setTextFill(Color.web("#555555"));

                            infoBox.getChildren().addAll(title, details);
                        }
                    }

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnRemove = createSmallButton("Remove", "#e74c3c");
                    btnRemove.setOnAction(e -> {
                        currentUser.getHistory().remove(item);
                        userHandler.saveUsers(USERS_FILE, allUsers);
                        refreshHistoryData();
                    });

                    HBox row = new HBox(10);
                    row.getChildren().addAll(infoBox, spacer, btnRemove);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                }
            }
        });

        layout.setCenter(globalHistoryListView);

        // Clear History Button
        Button btnClear = createStyledButton("Clear All History", "#c0392b");
        Label statusLabel = new Label();
        statusLabel.setPadding(new Insets(0,0,0,10));

        btnClear.setOnAction(e -> {
            if (currentUser.getHistory().isEmpty()) {
                updateStatus(statusLabel, "History is already empty.", Color.ORANGE);
            } else {
                currentUser.getHistory().clear();
                userHandler.saveUsers(USERS_FILE, allUsers);
                refreshHistoryData();
                updateStatus(statusLabel, "History cleared.", Color.GREEN);
            }
        });

        HBox bottomBox = new HBox(10);
        bottomBox.getChildren().addAll(btnClear, statusLabel);
        bottomBox.setPadding(new Insets(10));
        layout.setBottom(bottomBox);

        return layout;
    }

    // --- VIEW: Recommendations (Updated) ---
    private Pane createRecommendationsView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        // Top Controls
        Label header = new Label("Get Recommendations");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        CheckBox cbGenre = new CheckBox("Genre");
        CheckBox cbYear = new CheckBox("Year");
        CheckBox cbRating = new CheckBox("Rating");
        cbGenre.setSelected(true); cbYear.setSelected(true); cbRating.setSelected(true);

        TextField numField = new TextField("5");
        numField.setPrefWidth(50);

        Button generateBtn = createStyledButton("Generate", "#8e44ad");
        Button addAllBtn = createStyledButton("Add All to Watchlist", "#27ae60");
        addAllBtn.setDisable(true);

        HBox controls = new HBox(15);
        controls.getChildren().addAll(new Label("Weights:"), cbGenre, cbYear, cbRating, new Label("Count:"), numField, generateBtn, addAllBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(header, controls);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        layout.setTop(topBox);

        // List
        globalRecListView = new ListView<>();
        layout.setCenter(globalRecListView);
        Label statusLabel = new Label("Click Generate to start.");
        layout.setBottom(statusLabel);

        // Logic
        generateBtn.setOnAction(e -> {
            try {
                int n = Integer.parseInt(numField.getText().trim());
                if (n <= 0) throw new NumberFormatException();

                RecommendationEngine engine = new RecommendationEngine(allMovies);
                ArrayList<RecommendationEngine.MovieScore> recs = engine.getRecommendations(
                        currentUser, n, cbGenre.isSelected(), cbYear.isSelected(), cbRating.isSelected()
                );

                if (recs.isEmpty()) {
                    updateStatus(statusLabel, "No recommendations found.", Color.ORANGE);
                    globalRecListView.setItems(FXCollections.observableArrayList());
                    addAllBtn.setDisable(true);
                } else {
                    globalRecListView.setItems(FXCollections.observableArrayList(recs));
                    updateStatus(statusLabel, "Found " + recs.size() + " movies.", Color.GREEN);
                    addAllBtn.setDisable(false);
                }
            } catch (NumberFormatException ex) {
                updateStatus(statusLabel, "Invalid number.", Color.RED);
            }
        });

        addAllBtn.setOnAction(e -> {
            int count = 0;
            for (RecommendationEngine.MovieScore ms : globalRecListView.getItems()) {
                if (!currentUser.getWatchlist().contains(ms.movie.getId())) {
                    currentUser.addToWatchlist(ms.movie.getId());
                    count++;
                }
            }
            userHandler.saveUsers(USERS_FILE, allUsers);
            updateStatus(statusLabel, "Added " + count + " movies to watchlist.", Color.GREEN);
        });

        // Cell Factory for Recommendations
        globalRecListView.setCellFactory(param -> new ListCell<RecommendationEngine.MovieScore>() {
            @Override
            protected void updateItem(RecommendationEngine.MovieScore item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    Button btnAdd = createSmallButton("Add to Watchlist", "#3498db");
                    btnAdd.setOnAction(e -> {
                        if (currentUser.getWatchlist().contains(item.movie.getId())) {
                            updateStatus(statusLabel, "Already in watchlist.", Color.ORANGE);
                        } else {
                            currentUser.addToWatchlist(item.movie.getId());
                            userHandler.saveUsers(USERS_FILE, allUsers);
                            updateStatus(statusLabel, "Added: " + item.movie.getTitle(), Color.GREEN);
                        }
                    });

                    VBox info = new VBox(5);

                    // [核心修改在这里]：为标题创建一个单独的 Label 并设置字体粗细
                    Label titleLabel = new Label("[" + item.movie.getId() + "] " + item.movie.getTitle());
                    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                    info.getChildren().addAll(
                            titleLabel,
                            new Label(String.format("Score: %.2f | Genre: %s | Year: %d | Rating: %.1f",
                                    item.score * 10, item.movie.getGenre(), item.movie.getYear(), item.movie.getRating()))
                    );

                    Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                    HBox row = new HBox(10);
                    row.getChildren().addAll(info, spacer, btnAdd);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(row);
                }
            }
        });

        return layout;
    }

    // --- VIEW: Change Password ---
    private Pane createChangePasswordView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_LEFT);

        Label header = new Label("Change Password");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        PasswordField oldF = new PasswordField(); oldF.setPromptText("Old Password");
        PasswordField newF = new PasswordField(); newF.setPromptText("New Password");
        PasswordField conF = new PasswordField(); conF.setPromptText("Confirm Password");

        Button saveBtn = createStyledButton("Save Changes", "#27ae60");
        Label msg = new Label();

        saveBtn.setOnAction(e -> {
            if (!currentUser.verifyPassword(oldF.getText())) {
                updateStatus(msg, "Incorrect old password.", Color.RED);
            } else if (newF.getText().isEmpty()) {
                updateStatus(msg, "Password cannot be empty.", Color.RED);
            } else if (!newF.getText().equals(conF.getText())) {
                updateStatus(msg, "New passwords do not match.", Color.RED);
            } else {
                currentUser.setPassword(newF.getText());
                userHandler.saveUsers(USERS_FILE, allUsers);
                updateStatus(msg, "Success!", Color.GREEN);
                oldF.clear(); newF.clear(); conF.clear();
            }
        });

        layout.getChildren().addAll(header,
                new Label("Current:"), createPasswordWithEye(oldF),
                new Label("New:"), createPasswordWithEye(newF),
                new Label("Confirm:"), createPasswordWithEye(conF),
                saveBtn, msg);
        return layout;
    }

    // ==========================================
    //              HELPER METHODS
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
        updateStatus(statusLabel, "Watched: " + movie.getTitle(), Color.GREEN);
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
        ArrayList<String> history = new ArrayList<>(currentUser.getHistory());
        Collections.reverse(history);
        globalHistoryListView.setItems(FXCollections.observableArrayList(history));
    }

    // --- UI Component Helpers (Factory Methods) ---

    private BorderPane createBaseLayout(String title, String initialStatus) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));
        Label header = new Label(title);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        layout.setTop(header);
        Label status = new Label(initialStatus);
        status.setPadding(new Insets(5));
        layout.setBottom(status);
        return layout;
    }

    private HBox createMovieRow(Movie movie, Button... actions) {
        VBox infoBox = new VBox(5);
        Label titleLbl = new Label("[" + movie.getId() + "] " + movie.getTitle());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label detailLbl = new Label(String.format("Genre: %s | Year: %d | Rating: %.1f",
                movie.getGenre(), movie.getYear(), movie.getRating()));
        detailLbl.setTextFill(Color.web("#555555"));
        infoBox.getChildren().addAll(titleLbl, detailLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(infoBox);
        row.getChildren().add(spacer);
        row.getChildren().addAll(actions);
        return row;
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setFont(Font.font("Arial", 14));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> { if (!text.equals("Logout")) btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;"); });
        btn.setOnMouseExited(e -> { if (!text.equals("Logout")) btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"); });
        return btn;
    }

    private Button createStyledButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;", colorHex));
        return btn;
    }

    private Button createSmallButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 11px;", colorHex));
        btn.setPrefWidth(120);
        return btn;
    }

    private void updateStatus(Label label, String text, Color color) {
        label.setText(text);
        label.setTextFill(color);
    }

    private StackPane createPasswordWithEye(PasswordField passField) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(passField.textProperty());
        textField.setPromptText(passField.getPromptText());
        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        toggleBtn.setFocusTraversable(false);
        textField.setVisible(false);

        toggleBtn.setOnAction(e -> {
            if (textField.isVisible()) {
                textField.setVisible(false);
                passField.setVisible(true);
            } else {
                textField.setVisible(true);
                passField.setVisible(false);
            }
        });
        StackPane pane = new StackPane(textField, passField, toggleBtn);
        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleBtn, new Insets(0, 5, 0, 0));
        return pane;
    }
}