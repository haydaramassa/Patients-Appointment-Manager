package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.service.UserService;
import com.clinic.patientappointmentfrontend.user.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class UsersController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<UserModel> usersTable;

    @FXML
    private TableColumn<UserModel, Long> idColumn;

    @FXML
    private TableColumn<UserModel, String> fullNameColumn;

    @FXML
    private TableColumn<UserModel, String> emailColumn;

    @FXML
    private TableColumn<UserModel, String> roleColumn;

    private final UserService userService = new UserService();
    private final ObservableList<UserModel> userItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "SECRETARY"));
        roleComboBox.setValue("SECRETARY");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.setItems(userItems);

        Tooltip passwordTooltip = new Tooltip(
                "Password must contain at least 8 characters, uppercase letter, lowercase letter, number, and special character."
        );
        passwordTooltip.setWrapText(true);
        passwordTooltip.setMaxWidth(320);
        Tooltip.install(passwordField, passwordTooltip);
    }

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        pageTitleLabel.setText("Users Management - " + fullName);
        loadUsers();
    }

    @FXML
    protected void onCreateUserClick() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        clearStatus();

        if (isBlank(fullName) || isBlank(email) || isBlank(password) || isBlank(role)) {
            showError("Please fill all fields");
            return;
        }

        fullName = fullName.trim();
        email = email.trim();

        if (fullName.length() < 3) {
            showError("Full name must be at least 3 characters");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (!isStrongPassword(password)) {
            showError("Password is not strong enough");
            return;
        }

        if (isEmailAlreadyUsed(email)) {
            showError("This email is already registered");
            return;
        }

        boolean created = userService.createUser(currentBasicAuthToken, fullName, email, password, role);

        if (created) {
            showSuccess("User created successfully");
            clearForm();
            loadUsers();
        } else {
            showError("Failed to create user. Please check the backend response.");
        }
    }

    @FXML
    protected void onRefreshClick() {
        loadUsers();
        showSuccess("Users list refreshed");
    }

    @FXML
    protected void onDeleteSelectedUserClick() {
        UserModel selectedUser = usersTable.getSelectionModel().getSelectedItem();

        clearStatus();

        if (selectedUser == null) {
            showError("Please select a user to delete");
            return;
        }

        String currentEmail = getCurrentLoggedInEmail();

        if (selectedUser.getEmail() != null
                && selectedUser.getEmail().equalsIgnoreCase(currentEmail)) {
            showError("You cannot delete your own account");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete selected user?");
        confirmation.setContentText(
                "Are you sure you want to delete this user?\n\n"
                        + "Name: " + selectedUser.getFullName() + "\n"
                        + "Email: " + selectedUser.getEmail() + "\n"
                        + "Role: " + selectedUser.getRole()
        );

        ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

        if (result != ButtonType.OK) {
            showError("Delete cancelled");
            return;
        }

        boolean deleted = userService.deleteUser(currentBasicAuthToken, selectedUser.getId());

        if (deleted) {
            showSuccess("User deleted successfully");
            loadUsers();
        } else {
            showError("Failed to delete user. You cannot delete the last admin.");
        }
    }

    @FXML
    protected void onBackClick() throws Exception {
        SceneManager.loadDashboardScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    private void loadUsers() {
        List<UserModel> users = userService.getAllUsers(currentBasicAuthToken);
        userItems.setAll(users);
        usersTable.setItems(userItems);
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue("SECRETARY");
    }

    private boolean isEmailAlreadyUsed(String email) {
        return userItems.stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email));
    }

    private boolean isStrongPassword(String password) {
        return calculatePasswordScore(password) >= 5;
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

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String getCurrentLoggedInEmail() {
        if (currentBasicAuthToken == null || currentBasicAuthToken.isBlank()) {
            return "";
        }

        try {
            String decoded = new String(
                    Base64.getDecoder().decode(currentBasicAuthToken),
                    StandardCharsets.UTF_8
            );

            int separatorIndex = decoded.indexOf(":");

            if (separatorIndex == -1) {
                return "";
            }

            return decoded.substring(0, separatorIndex);

        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private void clearStatus() {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.setText("Ready");
    }

    private void showSuccess(String message) {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("status-success");
    }

    private void showError(String message) {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("status-error");
    }
}