package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.service.UserService;
import com.clinic.patientappointmentfrontend.user.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

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

    @FXML
    private TableColumn<UserModel, Void> actionsColumn;

    private final UserService userService = new UserService();
    private final ObservableList<UserModel> userItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    private Long editingUserId = null;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "SECRETARY"));
        roleComboBox.setValue("SECRETARY");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.setItems(userItems);

        setupActionsColumn();

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

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button resetButton = new Button("Reset Password");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionsBox = new HBox(8, editButton, resetButton, deleteButton);

            {
                actionsBox.setStyle("-fx-alignment: center-left;");

                editButton.setPrefWidth(75);
                resetButton.setPrefWidth(145);
                deleteButton.setPrefWidth(85);

                editButton.getStyleClass().add("table-action-button");
                resetButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().add("table-danger-button");

                editButton.setOnAction(event -> {
                    UserModel user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                resetButton.setOnAction(event -> {
                    UserModel user = getTableView().getItems().get(getIndex());
                    resetPassword(user);
                });

                deleteButton.setOnAction(event -> {
                    UserModel user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsBox);
                }
            }
        });
    }

    @FXML
    protected void onCreateUserClick() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        clearStatus();

        if (editingUserId != null) {
            showError("You are editing a user. Click Clear Form before creating a new user.");
            return;
        }

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

        if (isEmailAlreadyUsed(email, null)) {
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

    private void editUser(UserModel selectedUser) {
        clearStatus();

        if (selectedUser == null) {
            showError("Please select a user to edit");
            return;
        }

        editingUserId = selectedUser.getId();

        fullNameField.setText(selectedUser.getFullName());
        emailField.setText(selectedUser.getEmail());
        roleComboBox.setValue(selectedUser.getRole());

        passwordField.clear();
        passwordField.setDisable(true);
        passwordField.setPromptText("Use Reset Password to change password");

        showSuccess("Editing user: " + selectedUser.getFullName());
    }

    @FXML
    protected void onUpdateUserClick() {
        clearStatus();

        if (editingUserId == null) {
            showError("Please click Edit on a user row first");
            return;
        }
        passwordField.clear();

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();

        if (isBlank(fullName) || isBlank(email) || isBlank(role)) {
            showError("Please fill full name, email, and role");
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

        if (isEmailAlreadyUsed(email, editingUserId)) {
            showError("This email is already registered");
            return;
        }

        boolean updated = userService.updateUser(currentBasicAuthToken, editingUserId, fullName, email, role);

        if (updated) {
            showSuccess("User updated successfully");
            clearForm();
            loadUsers();
        } else {
            showError("Failed to update user");
        }
    }

    @FXML
    protected void onClearFormClick() {
        clearForm();
        showSuccess("Form cleared");
    }

    @FXML
    protected void onRefreshClick() {
        loadUsers();
        showSuccess("Users list refreshed");
    }

    private void resetPassword(UserModel selectedUser) {
        clearStatus();

        if (selectedUser == null) {
            showError("Please select a user to reset password");
            return;
        }

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        Label hintLabel = new Label(
                "Password must contain at least 8 characters, uppercase, lowercase, number, and special character."
        );
        hintLabel.setWrapText(true);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("User: " + selectedUser.getFullName()),
                new Label("Email: " + selectedUser.getEmail()),
                newPasswordField,
                confirmPasswordField,
                hintLabel
        );

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for selected user");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            showError("Reset password cancelled");
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (isBlank(newPassword) || isBlank(confirmPassword)) {
            showError("Please enter and confirm the new password");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            showError("Password is not strong enough");
            return;
        }

        boolean updated = userService.resetPassword(currentBasicAuthToken, selectedUser.getId(), newPassword);

        if (updated) {
            showSuccess("Password reset successfully");
        } else {
            showError("Failed to reset password");
        }
    }

    private void deleteUser(UserModel selectedUser) {
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
            clearForm();
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
        editingUserId = null;

        fullNameField.clear();
        emailField.clear();
        passwordField.clear();

        passwordField.setDisable(false);
        passwordField.setPromptText("Password for new user only");

        roleComboBox.setValue("SECRETARY");
        usersTable.getSelectionModel().clearSelection();
    }

    private boolean isEmailAlreadyUsed(String email, Long currentUserId) {
        return userItems.stream()
                .anyMatch(user ->
                        user.getEmail() != null
                                && user.getEmail().equalsIgnoreCase(email)
                                && (currentUserId == null || !user.getId().equals(currentUserId))
                );
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