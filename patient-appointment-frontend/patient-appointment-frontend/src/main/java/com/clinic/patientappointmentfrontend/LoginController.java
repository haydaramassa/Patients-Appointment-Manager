package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        statusLabel.getStyleClass().removeAll("status-success", "status-error");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            statusLabel.setText("Please enter email and password");
            statusLabel.getStyleClass().add("status-error");
            return;
        }

        String result = authService.login(email, password);

        if (result.startsWith("SUCCESS:")) {
            statusLabel.setText("Login successful");
            statusLabel.getStyleClass().add("status-success");

            try {
                String response = result.substring("SUCCESS:".length());

                String fullName = extractValue(response, "fullName");
                String role = extractValue(response, "role");
                String basicAuthToken = authService.encodeBasicAuth(email, password);

                SceneManager.loadDashboardScene(fullName, role, basicAuthToken);
            } catch (Exception e) {
                statusLabel.setText("Failed to open dashboard");
                statusLabel.getStyleClass().add("status-error");
            }
        } else {
            statusLabel.setText("Login failed");
            statusLabel.getStyleClass().add("status-error");
        }
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
}