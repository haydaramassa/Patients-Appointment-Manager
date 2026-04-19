package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.dto.DashboardSummaryResponse;
import com.clinic.patientappointmentfrontend.service.DashboardService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("login-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Login");
        primaryStage.show();
    }

    public static void loadDashboardScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("dashboard-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        DashboardController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        DashboardService dashboardService = new DashboardService();
        DashboardSummaryResponse summary = dashboardService.getSummary(basicAuthToken);

        if (summary != null) {
            controller.setSummaryData(
                    summary.getTotalPatients(),
                    summary.getTotalAppointments(),
                    summary.getTodayAppointments(),
                    summary.getTotalUsers()
            );
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Dashboard");
        primaryStage.show();
    }

    public static void loadPatientsScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("patients-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1100, 650);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        PatientsController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Patients");
        primaryStage.show();
    }

    public static void loadAppointmentsScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("appointments-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1150, 650);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        AppointmentsController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Appointments");
        primaryStage.show();
    }

    public static void loadUsersScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("users-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 700);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        UsersController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Users");
        primaryStage.show();
    }
}