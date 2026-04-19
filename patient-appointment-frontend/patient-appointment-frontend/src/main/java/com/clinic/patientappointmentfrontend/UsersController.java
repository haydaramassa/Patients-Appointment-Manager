package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.service.UserService;
import com.clinic.patientappointmentfrontend.user.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

        statusLabel.getStyleClass().removeAll("status-success", "status-error");

        if (isBlank(fullName) || isBlank(email) || isBlank(password) || isBlank(role)) {
            statusLabel.setText("Please fill all fields");
            statusLabel.getStyleClass().add("status-error");
            return;
        }

        boolean created = userService.createUser(currentBasicAuthToken, fullName, email, password, role);

        if (created) {
            statusLabel.setText("User created successfully");
            statusLabel.getStyleClass().add("status-success");

            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
            roleComboBox.setValue("SECRETARY");

            loadUsers();
        } else {
            statusLabel.setText("Failed to create user");
            statusLabel.getStyleClass().add("status-error");
        }
    }

    @FXML
    protected void onRefreshClick() {
        loadUsers();
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}