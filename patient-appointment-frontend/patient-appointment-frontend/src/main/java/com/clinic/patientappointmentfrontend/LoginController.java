package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.service.AuthService;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private Pane animationPane;

    @FXML
    private VBox loginBox;

    @FXML
    private VBox registerBox;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField registerFullNameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ProgressBar passwordStrengthBar;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label registerStatusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {


        Tooltip passwordTooltip = new Tooltip(
                "Password must contain at least 8 characters, uppercase letter, lowercase letter, number, and special character."
        );
        passwordTooltip.setWrapText(true);
        passwordTooltip.setMaxWidth(320);

        Tooltip.install(passwordStrengthLabel, passwordTooltip);
        Tooltip.install(passwordStrengthBar, passwordTooltip);
        Tooltip.install(registerPasswordField, passwordTooltip);

        registerPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        Platform.runLater(this::createMedicalAnimatedBackground);
    }

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        clearStatus(statusLabel);

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError(statusLabel, "Please enter email and password");
            return;
        }

        String result = authService.login(email.trim(), password);

        if (result.startsWith("SUCCESS:")) {
            showSuccess(statusLabel, "Login successful");

            try {
                String response = result.substring("SUCCESS:".length());

                String fullName = extractValue(response, "fullName");
                String role = extractValue(response, "role");
                String basicAuthToken = authService.encodeBasicAuth(email.trim(), password);

                SceneManager.loadDashboardScene(fullName, role, basicAuthToken);
            } catch (Exception e) {
                showError(statusLabel, "Failed to open dashboard");
            }
        } else {
            showError(statusLabel, "Login failed. Please check your email or password.");
        }
    }

    @FXML
    protected void onRegisterClick() {
        String fullName = registerFullNameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = "SECRETARY";

        clearStatus(registerStatusLabel);

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            showError(registerStatusLabel, "Please fill in all fields");
            return;
        }

        if (!isValidEmail(email.trim())) {
            showError(registerStatusLabel, "Please enter a valid email address");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(registerStatusLabel, "Passwords do not match");
            return;
        }

        if (!isStrongPassword(password)) {
            showError(registerStatusLabel, "Password is not strong enough");
            return;
        }

        String result = authService.register(fullName.trim(), email.trim(), password, role);

        if (result.startsWith("SUCCESS:")) {
            String registeredEmail = email.trim();

            clearRegisterFields();
            showLoginForm();

            emailField.setText(registeredEmail);
            showSuccess(statusLabel, "Account created successfully. Please login.");
        } else {
            showError(registerStatusLabel, getReadableRegisterError(result));
        }
    }

    @FXML
    protected void showRegisterForm() {
        clearLoginFields();

        loginBox.setVisible(false);
        loginBox.setManaged(false);

        registerBox.setVisible(true);
        registerBox.setManaged(true);

        clearStatus(registerStatusLabel);
        updatePasswordStrength("");
    }

    @FXML
    protected void showLoginForm() {
        clearRegisterFields();

        registerBox.setVisible(false);
        registerBox.setManaged(false);

        loginBox.setVisible(true);
        loginBox.setManaged(true);

        clearStatus(statusLabel);
    }

    private void updatePasswordStrength(String password) {
        passwordStrengthLabel.getStyleClass().removeAll(
                "password-label-weak",
                "password-label-medium",
                "password-label-strong"
        );

        if (password == null || password.isBlank()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthBar.setStyle("");
            passwordStrengthLabel.setText("Password strength");
            return;
        }

        int score = calculatePasswordScore(password);

        if (score <= 2) {
            passwordStrengthBar.setProgress(0.33);
            passwordStrengthBar.setStyle("-fx-accent: #e74c3c;");
            passwordStrengthLabel.setText("Weak password");
            passwordStrengthLabel.getStyleClass().add("password-label-weak");
        } else if (score <= 4) {
            passwordStrengthBar.setProgress(0.66);
            passwordStrengthBar.setStyle("-fx-accent: #f39c12;");
            passwordStrengthLabel.setText("Medium password");
            passwordStrengthLabel.getStyleClass().add("password-label-medium");
        } else {
            passwordStrengthBar.setProgress(1.0);
            passwordStrengthBar.setStyle("-fx-accent: #1f7a42;");
            passwordStrengthLabel.setText("Strong password");
            passwordStrengthLabel.getStyleClass().add("password-label-strong");
        }
    }

    private int calculatePasswordScore(String password) {
        int score = 0;

        if (password.length() >= 8) {
            score++;
        }

        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        if (password.matches(".*[a-z].*")) {
            score++;
        }

        if (password.matches(".*\\d.*")) {
            score++;
        }

        if (password.matches(".*[^A-Za-z0-9].*")) {
            score++;
        }

        return score;
    }

    private boolean isStrongPassword(String password) {
        return calculatePasswordScore(password) >= 5;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void clearLoginFields() {
        emailField.clear();
        passwordField.clear();
        clearStatus(statusLabel);
    }

    private void clearRegisterFields() {
        registerFullNameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
        confirmPasswordField.clear();
        updatePasswordStrength("");
        clearStatus(registerStatusLabel);
    }

    private String getReadableRegisterError(String result) {
        if (result == null || result.isBlank()) {
            return "Registration failed. Please try again.";
        }

        String error = result.replace("ERROR:", "").trim();

        if (error.toLowerCase().contains("already")
                || error.toLowerCase().contains("duplicate")
                || error.toLowerCase().contains("exists")) {
            return "This email is already registered.";
        }

        if (error.contains("Cannot connect to backend")) {
            return "Cannot connect to backend. Please make sure the server is running.";
        }

        if (error.toLowerCase().contains("role")) {
            return "Please select a valid role.";
        }

        return "Registration failed. Please check your information.";
    }

    private void clearStatus(Label label) {
        label.getStyleClass().removeAll("status-success", "status-error");
        label.setText("Ready");
    }

    private void showSuccess(Label label, String message) {
        label.getStyleClass().removeAll("status-success", "status-error");
        label.setText(message);
        label.getStyleClass().add("status-success");
    }

    private void showError(Label label, String message) {
        label.getStyleClass().removeAll("status-success", "status-error");
        label.setText(message);
        label.getStyleClass().add("status-error");
    }

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);

        if (start == -1) {
            return "";
        }

        start += search.length();

        int end = json.indexOf("\"", start);

        if (end == -1) {
            return "";
        }

        return json.substring(start, end);
    }

    private void createMedicalAnimatedBackground() {
        if (animationPane == null) {
            return;
        }

        animationPane.getChildren().clear();

        /*
         * هذه النسخة فيها قسمين:
         * 1) عناصر عامة بتضل تسبح تحت الكرت الأبيض وحواليه.
         * 2) عناصر إضافية مركزة بالجهة الفاضية اليسار.
         */

        // =========================
        // 1) Existing full background animation
        // =========================

        addAnimatedNode(createSoftCircle(90, 100, 80), 0, 0, 35, 28, 6, 0.04, 0.13);
        addAnimatedNode(createSoftCircle(250, 430, 95), 0, 0, -30, 34, 7, 0.03, 0.11);
        addAnimatedNode(createSoftCircle(520, 105, 70), 0, 0, 32, 24, 5, 0.04, 0.12);
        addAnimatedNode(createSoftCircle(760, 420, 115), 0, 0, 40, -30, 7, 0.03, 0.12);
        addAnimatedNode(createSoftCircle(940, 170, 75), 0, 0, -35, 26, 6, 0.03, 0.10);

        addAnimatedNode(createMedicalCross(), 75, 350, 30, -35, 5, 0.12, 0.34);
        addAnimatedNode(createPill(), 190, 165, -40, 30, 6, 0.14, 0.36);
        addAnimatedNode(createSyringe(), 330, 430, 35, -28, 7, 0.13, 0.32);
        addAnimatedNode(createHeartbeat(), 390, 285, -35, 32, 5, 0.10, 0.30);

        addAnimatedNode(createMedicalCross(), 510, 130, -30, 38, 6, 0.10, 0.30);
        addAnimatedNode(createPill(), 575, 400, 38, -30, 7, 0.10, 0.30);
        addAnimatedNode(createSyringe(), 640, 250, -32, 26, 6, 0.10, 0.28);
        addAnimatedNode(createHeartbeat(), 710, 90, 28, -26, 5, 0.08, 0.24);

        addAnimatedNode(createMedicalCross(), 840, 330, -26, 32, 6, 0.09, 0.26);
        addAnimatedNode(createPill(), 930, 210, 34, -24, 7, 0.10, 0.28);
        addAnimatedNode(createSyringe(), 1010, 455, -30, 34, 7, 0.09, 0.24);
        addAnimatedNode(createHeartbeat(), 1040, 115, 30, 28, 6, 0.08, 0.22);

        // =========================
        // 2) Extra drawings in the empty left side
        // =========================

        addAnimatedNode(createSmallCross(), 55, 165, 20, -18, 5, 0.10, 0.28);
        addAnimatedNode(createSmallCross(), 135, 515, -22, 18, 6, 0.09, 0.25);
        addAnimatedNode(createSmallCross(), 270, 255, 18, 20, 5, 0.08, 0.23);
        addAnimatedNode(createSmallCross(), 460, 475, -18, -18, 6, 0.08, 0.22);

        addAnimatedNode(createPill(), 70, 420, 28, -22, 7, 0.08, 0.23);
        addAnimatedNode(createPill(), 355, 120, -26, 20, 6, 0.08, 0.22);
        addAnimatedNode(createPill(), 455, 335, 24, -24, 7, 0.07, 0.20);

        addAnimatedNode(createSyringe(), 135, 275, -22, 26, 6, 0.075, 0.22);
        addAnimatedNode(createSyringe(), 485, 205, 24, -20, 7, 0.07, 0.20);

        addAnimatedNode(createHeartbeat(), 85, 90, 24, 18, 5, 0.07, 0.20);
        addAnimatedNode(createHeartbeat(), 245, 370, -24, -20, 6, 0.075, 0.22);
    }

    private Circle createSoftCircle(double x, double y, double radius) {
        Circle circle = new Circle(radius);
        circle.setLayoutX(x);
        circle.setLayoutY(y);
        circle.setFill(Color.web("#2F80ED", 0.10));
        circle.setEffect(new GaussianBlur(18));
        return circle;
    }

    private Group createMedicalCross() {
        Rectangle vertical = new Rectangle(14, 46);
        vertical.setArcWidth(8);
        vertical.setArcHeight(8);
        vertical.setFill(Color.web("#2F80ED"));

        Rectangle horizontal = new Rectangle(46, 14);
        horizontal.setArcWidth(8);
        horizontal.setArcHeight(8);
        horizontal.setFill(Color.web("#2F80ED"));

        vertical.setLayoutX(16);
        vertical.setLayoutY(0);

        horizontal.setLayoutX(0);
        horizontal.setLayoutY(16);

        Group group = new Group(vertical, horizontal);
        group.setOpacity(0.16);
        return group;
    }

    private Group createPill() {
        Rectangle pill = new Rectangle(76, 30);
        pill.setArcWidth(30);
        pill.setArcHeight(30);
        pill.setFill(Color.web("#34A853", 0.75));
        pill.setStroke(Color.web("#FFFFFF", 0.75));
        pill.setStrokeWidth(2);

        Line middleLine = new Line(38, 4, 38, 26);
        middleLine.setStroke(Color.web("#FFFFFF", 0.85));
        middleLine.setStrokeWidth(2);

        Group group = new Group(pill, middleLine);
        group.setRotate(-22);
        group.setOpacity(0.20);
        return group;
    }

    private Group createSmallCross() {
        Rectangle vertical = new Rectangle(8, 28);
        vertical.setArcWidth(6);
        vertical.setArcHeight(6);
        vertical.setFill(Color.web("#2F80ED"));

        Rectangle horizontal = new Rectangle(28, 8);
        horizontal.setArcWidth(6);
        horizontal.setArcHeight(6);
        horizontal.setFill(Color.web("#2F80ED"));

        vertical.setLayoutX(10);
        vertical.setLayoutY(0);

        horizontal.setLayoutX(0);
        horizontal.setLayoutY(10);

        Group group = new Group(vertical, horizontal);
        group.setOpacity(0.14);
        return group;
    }

    private Group createSyringe() {
        Rectangle body = new Rectangle(14, 58);
        body.setArcWidth(8);
        body.setArcHeight(8);
        body.setFill(Color.web("#2F80ED", 0.55));

        Line needle = new Line(7, 58, 7, 88);
        needle.setStroke(Color.web("#12385B", 0.65));
        needle.setStrokeWidth(2);

        Line top = new Line(-8, 0, 22, 0);
        top.setStroke(Color.web("#12385B", 0.55));
        top.setStrokeWidth(3);

        Line push = new Line(7, -22, 7, 0);
        push.setStroke(Color.web("#12385B", 0.55));
        push.setStrokeWidth(3);

        Line mark1 = new Line(3, 15, 11, 15);
        Line mark2 = new Line(3, 28, 11, 28);
        Line mark3 = new Line(3, 41, 11, 41);

        mark1.setStroke(Color.WHITE);
        mark2.setStroke(Color.WHITE);
        mark3.setStroke(Color.WHITE);

        Group group = new Group(body, needle, top, push, mark1, mark2, mark3);
        group.setRotate(45);
        group.setOpacity(0.18);
        return group;
    }

    private Group createHeartbeat() {
        Polyline line = new Polyline(
                0.0, 30.0,
                28.0, 30.0,
                42.0, 12.0,
                58.0, 48.0,
                74.0, 22.0,
                92.0, 30.0,
                130.0, 30.0
        );

        line.setStroke(Color.web("#EB5757", 0.75));
        line.setStrokeWidth(4);
        line.setFill(null);

        Circle dot = new Circle(5);
        dot.setLayoutX(135);
        dot.setLayoutY(30);
        dot.setFill(Color.web("#EB5757", 0.75));

        Group group = new Group(line, dot);
        group.setOpacity(0.16);
        return group;
    }

    private void addAnimatedNode(Node node,
                                 double layoutX,
                                 double layoutY,
                                 double moveX,
                                 double moveY,
                                 double durationSeconds,
                                 double fromOpacity,
                                 double toOpacity) {

        node.setLayoutX(layoutX);
        node.setLayoutY(layoutY);
        node.setMouseTransparent(true);
        node.setOpacity(fromOpacity);

        animationPane.getChildren().add(node);

        TranslateTransition move = new TranslateTransition(Duration.seconds(durationSeconds), node);
        move.setFromX(-moveX);
        move.setToX(moveX);
        move.setFromY(-moveY);
        move.setToY(moveY);
        move.setAutoReverse(true);
        move.setCycleCount(Animation.INDEFINITE);

        RotateTransition rotate = new RotateTransition(Duration.seconds(durationSeconds + 4), node);
        rotate.setFromAngle(-10);
        rotate.setToAngle(10);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(Animation.INDEFINITE);

        FadeTransition fade = new FadeTransition(Duration.seconds(durationSeconds / 2), node);
        fade.setFromValue(fromOpacity);
        fade.setToValue(toOpacity);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);

        ParallelTransition animation = new ParallelTransition(move, rotate, fade);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }
}