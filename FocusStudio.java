import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Focus Studio Professional Edition - v3.2 (Deadline & AI Focus Update)
 * Features: Login, User-specific tasks (Sort by Due Date), AI Deadline Reminder, Pro Timer, PDF Support.
 */
public class FocusStudio extends Application {

    // --- User Management Mock (Simulated Cloud Database) ---
    private static class UserAccount {
        String username;
        String password;
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        ObservableList<String> notes = FXCollections.observableArrayList();
        UserAccount(String u, String p) { this.username = u; this.password = p; }
    }
    private static Map<String, UserAccount> database = new HashMap<>();
    private UserAccount currentUser = null;

    // --- State Management ---
    private boolean isDarkMode = true;
    private int timeLeft = 1500; 
    private Timeline pomodoroTimeline;
    private boolean isPaused = false;
    
    // --- UI Components ---
    private Stage mainStage;
    private BorderPane mainLayout;
    private Label clockLabel;
    private Label timerLabel;
    private TextArea textbookArea;

    // --- Design Tokens ---
    private final String DARK_BG = "#121212";
    private final String DARK_CARD = "#1E1E1E";
    private final String DARK_TEXT = "#FFFFFF";
    private final String LIGHT_BG = "#FDFDFD";
    private final String LIGHT_CARD = "#FFFFFF";
    private final String LIGHT_TEXT = "#202124";
    private final String ACCENT = "#BB86FC"; 

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        
        // Default Admin User for testing
        UserAccount demo = new UserAccount("admin", "1234");
        demo.tasks.add(new Task("စာမေးပွဲအတွက် ပြင်ဆင်ရန်", LocalDate.now().plusDays(2).toString(), "Pending"));
        demo.tasks.add(new Task("Assignment တင်ရန်", LocalDate.now().plusDays(5).toString(), "Pending"));
        database.put("admin", demo);

        showLoginScreen();
        primaryStage.setTitle("Focus Studio Pro Max - v3.2");
        primaryStage.show();
    }

    // --- AUTHENTICATION SCREENS ---

    private void showLoginScreen() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + DARK_BG + ";");

        Label brand = new Label("FOCUS STUDY PLANNER");
        brand.setFont(Font.font("System", FontWeight.BOLD, 36));
        brand.setTextFill(Color.web(ACCENT));

        VBox loginCard = new VBox(20);
        loginCard.setMaxWidth(400);
        loginCard.setPadding(new Insets(40));
        loginCard.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 8);");

        Label title = new Label("စာပြန်ဖတ်ဖို့ အဆင်သင့်ပဲလား?");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-background-color: #2D2D2D; -fx-text-fill: white;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-background-color: #2D2D2D; -fx-text-fill: white;");

        Button loginBtn = new Button("LOGIN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");
        
        Label statusMsg = new Label("");
        statusMsg.setTextFill(Color.web("#CF6679"));

        loginBtn.setOnAction(e -> {
            String u = userField.getText();
            String p = passField.getText();
            if (database.containsKey(u) && database.get(u).password.equals(p)) {
                currentUser = database.get(u);
                initializeMainApp();
            } else {
                statusMsg.setText("အချက်အလက် မှားယွင်းနေပါသည်။");
            }
        });

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);
        Label ask = new Label("အကောင့်မရှိသေးဘူးလား?");
        ask.setTextFill(Color.LIGHTGRAY);
        Hyperlink signUpLink = new Hyperlink("Sign Up");
        signUpLink.setTextFill(Color.web(ACCENT));
        signUpLink.setOnAction(e -> showSignUpScreen());
        footer.getChildren().addAll(ask, signUpLink);

        loginCard.getChildren().addAll(title, userField, passField, loginBtn, statusMsg, footer);
        root.getChildren().addAll(brand, loginCard);

        Scene scene = new Scene(root, 1300, 850);
        mainStage.setScene(scene);
    }

    private void showSignUpScreen() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + DARK_BG + ";");
        root.setPadding(new Insets(50));

        VBox card = new VBox(20);
        card.setMaxWidth(400);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 20;");

        Label title = new Label("အကောင့်သစ်ဖွင့်ရန်");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        TextField uField = new TextField(); uField.setPromptText("နာမည်အသစ်ပေးပါ");
        uField.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-background-color: #2D2D2D; -fx-text-fill: white;");
        
        PasswordField pField = new PasswordField(); pField.setPromptText("စကားဝှက်ပေးပါ");
        pField.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-background-color: #2D2D2D; -fx-text-fill: white;");

        Button regBtn = new Button("CREATE ACCOUNT");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setStyle("-fx-background-color: #03DAC6; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10;");

        regBtn.setOnAction(e -> {
            String u = uField.getText();
            if(!u.isEmpty() && !database.containsKey(u)) {
                database.put(u, new UserAccount(u, pField.getText()));
                showLoginScreen();
            }
        });

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray;");
        backBtn.setOnAction(e -> showLoginScreen());

        card.getChildren().addAll(title, uField, pField, regBtn, backBtn);
        root.getChildren().add(card);
        mainStage.getScene().setRoot(root);
    }

    // --- MAIN APP ---

    private void initializeMainApp() {
        mainLayout = new BorderPane();
        mainLayout.setLeft(createSidebar());
        mainLayout.setTop(createTopBar());
        navigateTo("Dashboard");
        mainStage.getScene().setRoot(mainLayout);
        applyTheme(mainStage.getScene());
        startGlobalClock();
    }

    private VBox createSidebar() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30, 15, 30, 15));
        box.setPrefWidth(240);
        Label brand = new Label("FOCUS STUDIO ⚡");
        brand.setFont(Font.font("System", FontWeight.BOLD, 22));
        brand.setTextFill(Color.web(ACCENT));
        brand.setPadding(new Insets(0, 0, 30, 10));

        box.getChildren().addAll(
            brand,
            createNavButton("📊 Dashboard", "Dashboard"),
            createNavButton("📝 Assignments", "Assignments"),
            createNavButton("⏱ Focus Timer", "Timer"),
            createNavButton("📚 Textbooks", "Textbooks"),
            createNavButton("📓 My Notes", "Notes"),
            new Separator(),
            createLogoutBtn()
        );
        return box;
    }

    private Button createLogoutBtn() {
        Button btn = new Button("🚪 Logout");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-text-fill: #CF6679; -fx-background-color: transparent; -fx-alignment: center-left; -fx-padding: 10; -fx-cursor: hand;");
        btn.setOnAction(e -> { currentUser = null; showLoginScreen(); });
        return btn;
    }

    private HBox createTopBar() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(15, 30, 15, 30));
        box.setAlignment(Pos.CENTER_RIGHT);
        clockLabel = new Label();
        clockLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        Label userLabel = new Label("👤 " + currentUser.username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");
        Button themeToggle = new Button("🌓 Mode");
        themeToggle.setOnAction(e -> { isDarkMode = !isDarkMode; applyTheme(mainLayout.getScene()); });
        box.getChildren().addAll(userLabel, clockLabel, themeToggle);
        return box;
    }

    private Button createNavButton(String text, String target) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        btn.setOnAction(e -> navigateTo(target));
        return btn;
    }

    private void navigateTo(String target) {
        switch (target) {
            case "Assignments": mainLayout.setCenter(createAssignmentsView()); break;
            case "Timer": mainLayout.setCenter(createTimerView()); break;
            case "Textbooks": mainLayout.setCenter(createTextbookView()); break;
            case "Notes": mainLayout.setCenter(createNotesView()); break;
            default: mainLayout.setCenter(createDashboardView()); break;
        }
    }

    // --- DASHBOARD VIEW (AI DEADLINE TRACKER) ---
    private VBox createDashboardView() {
        VBox box = new VBox(25); box.setPadding(new Insets(40));
        Label welcome = new Label("မင်္ဂလာပါ " + currentUser.username + "! 🎓");
        welcome.setFont(Font.font("System", FontWeight.BOLD, 32));
        welcome.setTextFill(Color.web(isDarkMode ? DARK_TEXT : LIGHT_TEXT));

        VBox aiPanel = new VBox(15);
        aiPanel.setPadding(new Insets(25));
        aiPanel.setStyle("-fx-background-color: " + (isDarkMode ? "#2D1B4D" : "#F3E5F5") + "; -fx-background-radius: 20;");
        Label aiTitle = new Label("🤖 AI DEADLINE REMINDER");
        aiTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");

        // AI Logic: Find the nearest deadline
        Optional<Task> nearestTask = currentUser.tasks.stream()
                .filter(t -> t.getStatus().equals("Pending"))
                .min(Comparator.comparing(Task::getDueDate));

        Label aiMessage = new Label();
        if (nearestTask.isPresent()) {
            LocalDate deadline = LocalDate.parse(nearestTask.get().getDueDate());
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
            String urgency = daysLeft < 0 ? "ကျော်လွန်သွားပါပြီ။" : (daysLeft == 0 ? "ဒီနေ့ နောက်ဆုံးပဲနော်!" : daysLeft + " ရက်ပဲ လိုပါတော့တယ်။ စာတွေရော ရနေပြီလား? စိတ်အေးအေးထားပြီးသေချာလေ့လာဖို့မမေ့နဲ့နော်။");
            aiMessage.setText("အနီးဆုံး Deadline: \"" + nearestTask.get().getName() + "\" လုပ်ဖို့ " + urgency);
            if(daysLeft <= 1) aiMessage.setTextFill(Color.web("#FF5252")); // Red alert
            else aiMessage.setTextFill(Color.web(isDarkMode ? DARK_TEXT : "#444444"));
        } else {
            aiMessage.setText("လက်ရှိတွင် လုပ်ဆောင်ရန် အရေးကြီး Task များ မရှိသေးပါ။ စာများကိုအေးအေးဆေးဆေး လေ့လာဖတ်ရှုနိုင်ပါသည်။");
            aiMessage.setTextFill(Color.web(isDarkMode ? DARK_TEXT : "#444444"));
        }
        
        aiPanel.getChildren().addAll(aiTitle, aiMessage);

        HBox stats = new HBox(20);
        long pendingCount = currentUser.tasks.stream().filter(t->t.getStatus().equals("Pending")).count();
        stats.getChildren().addAll(
            createStatCard("Pending Tasks", String.valueOf(pendingCount)),
            createStatCard("Notes Saved", String.valueOf(currentUser.notes.size()))
        );
        box.getChildren().addAll(welcome, aiPanel, stats);
        return box;
    }

    // --- ASSIGNMENTS VIEW (SMART SORTING & DEADLINES) ---
    private VBox createAssignmentsView() {
        VBox box = new VBox(20); box.setPadding(new Insets(40));
        Label title = new Label("Assignment စာရင်း (Deadlines စီထားသည်)");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        // Smart Sorting Logic: Pending items first, then sort by date
        SortedList<Task> sortedData = new SortedList<>(currentUser.tasks, (t1, t2) -> {
            if (t1.getStatus().equals(t2.getStatus())) {
                return t1.getDueDate().compareTo(t2.getDueDate());
            }
            return t1.getStatus().equals("Pending") ? -1 : 1;
        });

        TableView<Task> table = new TableView<>(sortedData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-control-inner-background: " + (isDarkMode ? DARK_CARD : LIGHT_CARD) + ";");

        TableColumn<Task, String> nameCol = new TableColumn<>("လုပ်ဆောင်ချက်");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Task, String> dateCol = new TableColumn<>("Deadline");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        
        TableColumn<Task, String> statusCol = new TableColumn<>("အခြေအနေ");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Task, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button doneBtn = new Button("Mark Done");
            {
                doneBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                doneBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    t.setStatus("Completed");
                    table.refresh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Task t = getTableView().getItems().get(getIndex());
                    setGraphic(t.getStatus().equals("Completed") ? new Label("✅ Done") : doneBtn);
                }
            }
        });

        table.getColumns().addAll(nameCol, dateCol, statusCol, actionCol);

        HBox inputs = new HBox(10);
        inputs.setAlignment(Pos.CENTER_LEFT);
        TextField tin = new TextField(); tin.setPromptText("Task အမည်...");
        DatePicker din = new DatePicker(LocalDate.now());
        Button addB = new Button("➕ Add New Task");
        addB.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        
        addB.setOnAction(e -> { 
            if(!tin.getText().isEmpty() && din.getValue() != null) { 
                currentUser.tasks.add(new Task(tin.getText(), din.getValue().toString(), "Pending")); 
                tin.clear(); 
            }
        });
        inputs.getChildren().addAll(tin, din, addB);

        box.getChildren().addAll(title, table, inputs);
        return box;
    }

    // --- PRO TIMER VIEW ---
    private VBox createTimerView() {
        VBox box = new VBox(40); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(40));
        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 140));
        timerLabel.setTextFill(Color.web(ACCENT));

        HBox controls = new HBox(15); controls.setAlignment(Pos.CENTER);
        Button startBtn = new Button("START");
        Button pauseBtn = new Button("PAUSE");
        Button stopBtn = new Button("RESET");

        String btnS = "-fx-min-width: 120; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;";
        startBtn.setStyle(btnS + "-fx-background-color: " + ACCENT + "; -fx-text-fill: white;");
        pauseBtn.setStyle(btnS + "-fx-background-color: #333; -fx-text-fill: white;");
        stopBtn.setStyle(btnS + "-fx-background-color: #CF6679; -fx-text-fill: white;");

        startBtn.setOnAction(e -> { 
            if (isPaused) { pomodoroTimeline.play(); isPaused = false; timerLabel.setTextFill(Color.web(ACCENT)); }
            else startFocusTimer(); 
        });
        pauseBtn.setOnAction(e -> { if(pomodoroTimeline != null) { pomodoroTimeline.pause(); isPaused = true; timerLabel.setTextFill(Color.YELLOW); }});
        stopBtn.setOnAction(e -> { if(pomodoroTimeline != null) pomodoroTimeline.stop(); timeLeft = 1500; isPaused = false; updateTimerDisplay(); timerLabel.setTextFill(Color.web(ACCENT)); });

        controls.getChildren().addAll(startBtn, pauseBtn, stopBtn);
        box.getChildren().addAll(new Label("DEEP FOCUS POMODORO"), timerLabel, controls);
        return box;
    }

    // --- TEXTBOOKS VIEW (PDF & TXT) ---
    private HBox createTextbookView() {
        HBox main = new HBox(20); main.setPadding(new Insets(25));
        VBox.setVgrow(main, Priority.ALWAYS);
        
        VBox left = new VBox(20); HBox.setHgrow(left, Priority.ALWAYS);
        textbookArea = new TextArea(); textbookArea.setEditable(false);
        textbookArea.setWrapText(true);
        VBox.setVgrow(textbookArea, Priority.ALWAYS);
        textbookArea.setStyle("-fx-control-inner-background: " + (isDarkMode ? DARK_CARD : LIGHT_CARD) + "; -fx-font-size: 15px;");
        
        Button openB = new Button("📁 Open Textbook (.txt / .pdf)");
        openB.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        openB.setOnAction(e -> loadFile());
        left.getChildren().addAll(openB, textbookArea);

        VBox right = new VBox(15); right.setPrefWidth(350);
        TextArea ni = new TextArea(); ni.setPromptText("စာဖတ်ရင်း မှတ်စရာရှိလျှင် ရေးရန်...");
        VBox.setVgrow(ni, Priority.ALWAYS);
        Button saveB = new Button("Save Note");
        saveB.setMaxWidth(Double.MAX_VALUE);
        saveB.setOnAction(e -> { if(!ni.getText().isEmpty()){ currentUser.notes.add(0, "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd")) + "] " + ni.getText()); ni.clear(); }});
        right.getChildren().addAll(new Label("STUDY NOTES"), ni, saveB);

        main.getChildren().addAll(left, right);
        return main;
    }

    private VBox createNotesView() {
        VBox box = new VBox(20); box.setPadding(new Insets(40));
        ListView<String> lv = new ListView<>(currentUser.notes);
        VBox.setVgrow(lv, Priority.ALWAYS);
        lv.setStyle("-fx-control-inner-background: " + (isDarkMode ? DARK_CARD : LIGHT_CARD) + ";");
        box.getChildren().addAll(new Label("သိမ်းဆည်းထားသော မှတ်စုများ"), lv);
        return box;
    }

    // --- HELPERS ---

    private void loadFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Support Files", "*.txt", "*.pdf"));
        File f = fc.showOpenDialog(null);
        if(f != null) {
            if (f.getName().endsWith(".txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    StringBuilder sb = new StringBuilder(); String l;
                    while((l = br.readLine()) != null) sb.append(l).append("\n");
                    textbookArea.setText(sb.toString());
                } catch (Exception ignored) {}
            } else if (f.getName().endsWith(".pdf")) {
                textbookArea.setText("PDF Viewer ကို အသုံးပြု၍ ဖွင့်လှစ်နေပါသည်။\nဖိုင်: " + f.getName());
                try { java.awt.Desktop.getDesktop().open(f); } catch (Exception ex) { textbookArea.setText("PDF ဖွင့်မရပါ။ Viewer ရှိမရှိ စစ်ဆေးပါ။"); }
            }
        }
    }

    private void startFocusTimer() {
        if(pomodoroTimeline != null) pomodoroTimeline.stop();
        timeLeft = 1500;
        pomodoroTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if(timeLeft > 0) { timeLeft--; updateTimerDisplay(); }
            else { pomodoroTimeline.stop(); showAlarm(); }
        }));
        pomodoroTimeline.setCycleCount(Animation.INDEFINITE);
        pomodoroTimeline.play();
        isPaused = false;
        timerLabel.setTextFill(Color.web(ACCENT));
    }

    private void updateTimerDisplay() { timerLabel.setText(String.format("%02d:%02d", timeLeft/60, timeLeft%60)); }
    private void showAlarm() { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Time's Up!"); a.setContentText("Focus session ပြီးဆုံးပါပြီ။ အနားယူလိုက်ပါဦး။"); a.show(); }

    private void startGlobalClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            if(clockLabel != null) clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE); clock.play();
    }

    private void applyTheme(Scene scene) {
        String bg = isDarkMode ? DARK_BG : LIGHT_BG;
        String text = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        mainLayout.setStyle("-fx-background-color: " + bg + ";");
        scene.getRoot().setStyle("-fx-base: " + bg + "; -fx-text-base-color: " + text + ";");
    }

    private StackPane createStatCard(String title, String val) {
        VBox box = new VBox(10); box.setAlignment(Pos.CENTER);
        box.setMinWidth(220); box.setMinHeight(140);
        box.setStyle("-fx-background-color: " + (isDarkMode ? DARK_CARD : "#FFFFFF") + "; -fx-background-radius: 15;");
        Label t = new Label(title); t.setTextFill(Color.GRAY);
        Label v = new Label(val); v.setFont(Font.font("System", FontWeight.BOLD, 28));
        v.setTextFill(isDarkMode ? Color.WHITE : Color.BLACK);
        box.getChildren().addAll(t, v);
        return new StackPane(box);
    }

    public static class Task {
        private final SimpleStringProperty name, dueDate, status;
        public Task(String n, String d, String s) {
            this.name = new SimpleStringProperty(n);
            this.dueDate = new SimpleStringProperty(d);
            this.status = new SimpleStringProperty(s);
        }
        public String getName() { return name.get(); }
        public String getDueDate() { return dueDate.get(); }
        public String getStatus() { return status.get(); }
        public void setStatus(String s) { status.set(s); }
    }

    public static void main(String[] args) { launch(args); }
}