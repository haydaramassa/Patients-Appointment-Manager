package com.clinic.patientappointmentfrontend;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label totalPatientsLabel;

    @FXML
    private Label totalAppointmentsLabel;

    @FXML
    private Label todayAppointmentsLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Button usersButton;

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        welcomeLabel.setText("Welcome, " + fullName);
        roleLabel.setText("Role: " + role);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            usersButton.setVisible(false);
            usersButton.setManaged(false);
        }
    }

    public void setSummaryData(long totalPatients, long totalAppointments, long todayAppointments, long totalUsers) {
        totalPatientsLabel.setText(String.valueOf(totalPatients));
        totalAppointmentsLabel.setText(String.valueOf(totalAppointments));
        todayAppointmentsLabel.setText(String.valueOf(todayAppointments));
        totalUsersLabel.setText(String.valueOf(totalUsers));
    }

    @FXML
    protected void onPatientsClick() throws Exception {
        SceneManager.loadPatientsScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    @FXML
    protected void onAppointmentsClick() throws Exception {
        SceneManager.loadAppointmentsScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    @FXML
    protected void onUsersClick() throws Exception {
        SceneManager.loadUsersScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    @FXML
    protected void onLogoutClick() throws Exception {
        SceneManager.loadLoginScene();
    }
}