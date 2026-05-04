package com.clinic.patientappointmentfrontend;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label dashboardTitleLabel;

    @FXML
    private Label clinicNameSidebarLabel;

    @FXML
    private Label clinicNameMainLabel;

    @FXML
    private Label clinicSubtitleLabel;

    @FXML
    private Label clinicPhoneLabel;

    @FXML
    private Label clinicAddressLabel;

    @FXML
    private Label clinicHoursLabel;

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

    @FXML
    public void initialize() {
        clinicNameSidebarLabel.setText(AppConfig.CLINIC_NAME);
        clinicNameMainLabel.setText(AppConfig.CLINIC_NAME);
        clinicSubtitleLabel.setText(AppConfig.APP_NAME);

        clinicPhoneLabel.setText("Phone: " + AppConfig.CLINIC_PHONE);
        clinicAddressLabel.setText("Address: " + AppConfig.CLINIC_ADDRESS);
        clinicHoursLabel.setText("Working Hours: " + AppConfig.WORKING_HOURS);

        dashboardTitleLabel.setText("Dashboard");
    }

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
    protected void onClinicInfoClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clinic Information");
        alert.setHeaderText(AppConfig.CLINIC_NAME);

        alert.setContentText(
                AppConfig.APP_NAME + "\n\n"
                        + "Phone: " + AppConfig.CLINIC_PHONE + "\n"
                        + "Address: " + AppConfig.CLINIC_ADDRESS + "\n"
                        + "Working Hours: " + AppConfig.WORKING_HOURS + "\n\n"
                        + "Version: " + AppConfig.APP_VERSION + "\n"
                        + "Developed by: " + AppConfig.DEVELOPER_NAME
        );

        alert.showAndWait();
    }

    @FXML
    protected void onLogoutClick() throws Exception {
        SceneManager.loadLoginScene();
    }
}