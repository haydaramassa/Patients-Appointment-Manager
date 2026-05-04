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

        Scene scene = createScene(loader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Login");
        primaryStage.show();
    }

    public static void loadDashboardScene(String fullName, String role, String basicAuthToken) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                ClinicApplication.class.getResource("dashboard-view.fxml")
        );

        Scene scene = createScene(loader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

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

        Scene scene = createScene(loader.load(), 1100, 650);

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

        Scene scene = createScene(loader.load(), 1150, 650);

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

        Scene scene = createScene(loader.load(), 1200, 700);

        UsersController controller = loader.getController();
        controller.setUserData(fullName, role, basicAuthToken);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Patient Appointment Manager - Users");
        primaryStage.show();
    }

    private static Scene createScene(Parent root, double defaultWidth, double defaultHeight) {
        WindowState windowState = captureWindowState();

        double width = windowState.width() > 0 ? windowState.width() : defaultWidth;
        double height = windowState.height() > 0 ? windowState.height() : defaultHeight;

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
                ClinicApplication.class.getResource("login.css").toExternalForm()
        );

        applyWindowState(windowState);

        return scene;
    }

    private static WindowState captureWindowState() {
        if (primaryStage == null || primaryStage.getScene() == null) {
            return new WindowState(0, 0, 0, 0, false);
        }

        return new WindowState(
                primaryStage.getX(),
                primaryStage.getY(),
                primaryStage.getWidth(),
                primaryStage.getHeight(),
                primaryStage.isMaximized()
        );
    }

    private static void applyWindowState(WindowState windowState) {
        if (primaryStage == null) {
            return;
        }

        primaryStage.setMaximized(false);

        if (windowState.x() > 0) {
            primaryStage.setX(windowState.x());
        }

        if (windowState.y() > 0) {
            primaryStage.setY(windowState.y());
        }

        if (windowState.width() > 0) {
            primaryStage.setWidth(windowState.width());
        }

        if (windowState.height() > 0) {
            primaryStage.setHeight(windowState.height());
        }

        primaryStage.setMaximized(windowState.maximized());
    }

    private record WindowState(
            double x,
            double y,
            double width,
            double height,
            boolean maximized
    ) {
    }
}