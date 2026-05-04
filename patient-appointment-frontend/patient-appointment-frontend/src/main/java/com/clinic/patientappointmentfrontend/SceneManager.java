package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.dto.DashboardSummaryResponse;
import com.clinic.patientappointmentfrontend.service.DashboardService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("login-view.fxml")
        );

        Parent root = loader.load();
        showRoot(root, DEFAULT_WIDTH, DEFAULT_HEIGHT, "Patient Appointment Manager - Login");
    }

    public static void loadDashboardScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("dashboard-view.fxml")
        );

        Parent root = loader.load();

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

        showRoot(root, DEFAULT_WIDTH, DEFAULT_HEIGHT, "Patient Appointment Manager - Dashboard");
    }

    public static void loadPatientsScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("patients-view.fxml")
        );

        Parent root = loader.load();

        PatientsController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        showRoot(root, 1100, 650, "Patient Appointment Manager - Patients");
    }

    public static void loadAppointmentsScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("appointments-view.fxml")
        );

        Parent root = loader.load();

        AppointmentsController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        showRoot(root, 1150, 650, "Patient Appointment Manager - Appointments");
    }

    public static void loadUsersScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("users-view.fxml")
        );

        Parent root = loader.load();

        UsersController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        showRoot(root, 1200, 700, "Patient Appointment Manager - Users");
    }

    private static void showRoot(Parent root, double defaultWidth, double defaultHeight, String title) {
        boolean wasMaximized = primaryStage.isMaximized();

        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();
        double currentWidth = primaryStage.getWidth() > 0 ? primaryStage.getWidth() : defaultWidth;
        double currentHeight = primaryStage.getHeight() > 0 ? primaryStage.getHeight() : defaultHeight;

        Scene currentScene = primaryStage.getScene();

        if (currentScene == null) {
            Scene scene = new Scene(root, defaultWidth, defaultHeight);
            scene.getStylesheets().add(
                    ClinicApplication.class.getResource("login.css").toExternalForm()
            );

            primaryStage.setScene(scene);
        } else {
            currentScene.setRoot(root);
        }

        primaryStage.setTitle(title);

        primaryStage.setX(currentX);
        primaryStage.setY(currentY);
        primaryStage.setWidth(currentWidth);
        primaryStage.setHeight(currentHeight);
        primaryStage.setMaximized(wasMaximized);

        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }
}