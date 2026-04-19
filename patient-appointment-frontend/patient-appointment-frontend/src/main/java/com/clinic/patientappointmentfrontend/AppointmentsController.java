package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.appointment.AppointmentModel;
import com.clinic.patientappointmentfrontend.service.AppointmentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<AppointmentModel> appointmentsTable;

    @FXML
    private TableColumn<AppointmentModel, Long> idColumn;

    @FXML
    private TableColumn<AppointmentModel, String> patientNameColumn;

    @FXML
    private TableColumn<AppointmentModel, String> dateTimeColumn;

    @FXML
    private TableColumn<AppointmentModel, String> statusColumn;

    @FXML
    private TableColumn<AppointmentModel, String> doctorNameColumn;

    private final AppointmentService appointmentService = new AppointmentService();
    private final ObservableList<AppointmentModel> appointmentItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDateTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));

        appointmentsTable.setItems(appointmentItems);
    }

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        pageTitleLabel.setText("Appointments - " + fullName);
        loadAppointments();
    }

    @FXML
    protected void onRefreshClick() {
        loadAppointments();
    }

    @FXML
    protected void onSearchClick() {
        String keyword = searchField.getText();

        if (keyword == null || keyword.isBlank()) {
            loadAppointments();
            return;
        }

        List<AppointmentModel> filtered = appointmentItems.stream()
                .filter(a ->
                        containsIgnoreCase(a.getPatientName(), keyword) ||
                                containsIgnoreCase(a.getDoctorName(), keyword) ||
                                containsIgnoreCase(a.getStatus(), keyword))
                .collect(Collectors.toList());

        appointmentsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onBackClick() throws Exception {
        SceneManager.loadDashboardScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    private void loadAppointments() {
        List<AppointmentModel> appointments = appointmentService.getAllAppointments(currentBasicAuthToken);
        appointmentItems.setAll(appointments);
        appointmentsTable.setItems(appointmentItems);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
}