package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.appointment.AppointmentModel;
import com.clinic.patientappointmentfrontend.patient.PatientModel;
import com.clinic.patientappointmentfrontend.service.AppointmentService;
import com.clinic.patientappointmentfrontend.service.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private ComboBox<PatientModel> patientComboBox;

    @FXML
    private DatePicker appointmentDatePicker;

    @FXML
    private TextField appointmentTimeField;

    @FXML
    private TextField doctorNameField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextArea notesField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<AppointmentModel> appointmentsTable;

    @FXML
    private TableColumn<AppointmentModel, String> patientNameColumn;

    @FXML
    private TableColumn<AppointmentModel, String> dateTimeColumn;

    @FXML
    private TableColumn<AppointmentModel, String> statusColumn;

    @FXML
    private TableColumn<AppointmentModel, String> doctorNameColumn;

    @FXML
    private TableColumn<AppointmentModel, String> notesColumn;

    @FXML
    private TableColumn<AppointmentModel, Void> actionsColumn;

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();

    private final ObservableList<AppointmentModel> appointmentItems = FXCollections.observableArrayList();
    private final ObservableList<PatientModel> patientItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    private Long editingAppointmentId = null;

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("SCHEDULED", "COMPLETED", "CANCELLED"));
        statusComboBox.setValue("SCHEDULED");

        setupPatientComboBox();
        disablePastAppointmentDates();

        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDateTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        setupLongTextColumn(notesColumn);
        setupActionsColumn();

        appointmentsTable.setItems(appointmentItems);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAppointments(newValue);
        });
    }

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        pageTitleLabel.setText("Appointments - " + fullName);

        loadPatients();
        loadAppointments();
    }

    private void setupPatientComboBox() {
        patientComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PatientModel patient) {
                if (patient == null) {
                    return "";
                }

                String fullName = safe(patient.getFirstName()) + " " + safe(patient.getLastName());
                String phone = safe(patient.getPhone());

                if (!phone.isBlank()) {
                    return fullName.trim() + " - " + phone;
                }

                return fullName.trim();
            }

            @Override
            public PatientModel fromString(String string) {
                return null;
            }
        });
    }

    private void disablePastAppointmentDates() {
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f7d6d6; -fx-text-fill: #999999;");
                }
            }
        });
    }

    private void setupLongTextColumn(TableColumn<AppointmentModel, String> column) {
        column.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null || value.isBlank()) {
                    setText("");
                    setTooltip(null);
                } else {
                    setText(value);
                    setWrapText(true);

                    Tooltip tooltip = new Tooltip(value);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(350);
                    setTooltip(tooltip);
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionsBox = new HBox(8, editButton, deleteButton);

            {
                actionsBox.setStyle("-fx-alignment: center-left;");

                editButton.setPrefWidth(75);
                deleteButton.setPrefWidth(85);

                editButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().add("table-danger-button");

                editButton.setOnAction(event -> {
                    AppointmentModel appointment = getTableView().getItems().get(getIndex());
                    editAppointment(appointment);
                });

                deleteButton.setOnAction(event -> {
                    AppointmentModel appointment = getTableView().getItems().get(getIndex());
                    deleteAppointment(appointment);
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
    protected void onCreateAppointmentClick() {
        clearStatus();

        if (editingAppointmentId != null) {
            showError("You are editing an appointment. Click Clear Form before creating a new appointment.");
            return;
        }

        AppointmentFormData formData = getFormData();

        if (!validateAppointmentForm(formData)) {
            return;
        }

        boolean created = appointmentService.createAppointment(
                currentBasicAuthToken,
                formData.patientId(),
                formData.appointmentDateTime(),
                formData.status(),
                formData.doctorName(),
                formData.notes()
        );

        if (created) {
            showSuccess("Appointment created successfully");
            clearForm();
            loadAppointments();
        } else {
            showError("Failed to create appointment. Please check the backend response.");
        }
    }

    private void editAppointment(AppointmentModel appointment) {
        clearStatus();

        if (appointment == null) {
            showError("Please select an appointment to edit");
            return;
        }

        editingAppointmentId = appointment.getId();

        selectPatientById(appointment.getPatientId());
        doctorNameField.setText(appointment.getDoctorName());
        statusComboBox.setValue(appointment.getStatus());
        notesField.setText(appointment.getNotes());

        setDateAndTimeFields(appointment.getAppointmentDateTime());

        showSuccess("Editing appointment for: " + appointment.getPatientName());
    }

    @FXML
    protected void onUpdateAppointmentClick() {
        clearStatus();

        if (editingAppointmentId == null) {
            showError("Please click Edit on an appointment row first");
            return;
        }

        AppointmentFormData formData = getFormData();

        if (!validateAppointmentForm(formData)) {
            return;
        }

        boolean updated = appointmentService.updateAppointment(
                currentBasicAuthToken,
                editingAppointmentId,
                formData.patientId(),
                formData.appointmentDateTime(),
                formData.status(),
                formData.doctorName(),
                formData.notes()
        );

        if (updated) {
            showSuccess("Appointment updated successfully");
            clearForm();
            loadAppointments();
        } else {
            showError("Failed to update appointment");
        }
    }

    private void deleteAppointment(AppointmentModel appointment) {
        clearStatus();

        if (appointment == null) {
            showError("Please select an appointment to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Appointment");
        confirmation.setHeaderText("Delete selected appointment?");
        confirmation.setContentText(
                "Are you sure you want to delete this appointment?\n\n"
                        + "Patient: " + appointment.getPatientName() + "\n"
                        + "Date/Time: " + appointment.getAppointmentDateTime() + "\n"
                        + "Status: " + appointment.getStatus()
        );

        ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

        if (result != ButtonType.OK) {
            showError("Delete cancelled");
            return;
        }

        boolean deleted = appointmentService.deleteAppointment(currentBasicAuthToken, appointment.getId());

        if (deleted) {
            showSuccess("Appointment deleted successfully");
            clearForm();
            loadAppointments();
        } else {
            showError("Failed to delete appointment");
        }
    }

    @FXML
    protected void onClearFormClick() {
        clearForm();
        showSuccess("Form cleared");
    }

    @FXML
    protected void onRefreshClick() {
        searchField.clear();
        loadPatients();
        loadAppointments();
        showSuccess("Appointments list refreshed");
    }

    @FXML
    protected void onSearchClick() {
        filterAppointments(searchField.getText());
    }

    private void filterAppointments(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            appointmentsTable.setItems(appointmentItems);
            return;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        List<AppointmentModel> filtered = appointmentItems.stream()
                .filter(appointment ->
                        containsIgnoreCase(appointment.getPatientName(), normalizedKeyword)
                                || containsIgnoreCase(appointment.getDoctorName(), normalizedKeyword)
                                || containsIgnoreCase(appointment.getStatus(), normalizedKeyword)
                                || containsIgnoreCase(appointment.getAppointmentDateTime(), normalizedKeyword)
                                || containsIgnoreCase(appointment.getNotes(), normalizedKeyword)
                )
                .collect(Collectors.toList());

        appointmentsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onBackClick() throws Exception {
        SceneManager.loadDashboardScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    private void loadPatients() {
        List<PatientModel> patients = patientService.getAllPatients(currentBasicAuthToken);
        patientItems.setAll(patients);
        patientComboBox.setItems(patientItems);
    }

    private void loadAppointments() {
        List<AppointmentModel> appointments = appointmentService.getAllAppointments(currentBasicAuthToken);
        appointmentItems.setAll(appointments);

        if (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) {
            filterAppointments(searchField.getText());
        } else {
            appointmentsTable.setItems(appointmentItems);
        }
    }

    private AppointmentFormData getFormData() {
        PatientModel selectedPatient = patientComboBox.getValue();
        Long patientId = selectedPatient == null ? null : selectedPatient.getId();

        String doctorName = safe(doctorNameField.getText()).trim();
        String status = safe(statusComboBox.getValue()).trim();
        String notes = safe(notesField.getText()).trim();

        String appointmentDateTime = "";

        if (appointmentDatePicker.getValue() != null && !safe(appointmentTimeField.getText()).isBlank()) {
            appointmentDateTime = appointmentDatePicker.getValue() + "T" + appointmentTimeField.getText().trim();
        }

        return new AppointmentFormData(patientId, appointmentDateTime, status, doctorName, notes);
    }

    private boolean validateAppointmentForm(AppointmentFormData formData) {
        if (formData.patientId() == null) {
            showError("Please select a patient");
            return false;
        }

        if (isBlank(formData.appointmentDateTime())) {
            showError("Please select appointment date and time");
            return false;
        }

        if (isBlank(formData.status())) {
            showError("Please select appointment status");
            return false;
        }

        if (!isValidTime(appointmentTimeField.getText())) {
            showError("Please enter time in HH:mm format");
            return false;
        }

        LocalDateTime appointmentDateTime;

        try {
            appointmentDateTime = LocalDateTime.parse(formData.appointmentDateTime());
        } catch (Exception e) {
            showError("Invalid appointment date/time");
            return false;
        }

        if ("SCHEDULED".equals(formData.status()) && appointmentDateTime.isBefore(LocalDateTime.now())) {
            showError("Scheduled appointment cannot be in the past");
            return false;
        }

        if (!isBlank(formData.doctorName()) && formData.doctorName().length() < 2) {
            showError("Doctor name must be at least 2 characters");
            return false;
        }

        return true;
    }

    private void clearForm() {
        editingAppointmentId = null;

        patientComboBox.setValue(null);
        appointmentDatePicker.setValue(null);
        appointmentTimeField.clear();
        doctorNameField.clear();
        statusComboBox.setValue("SCHEDULED");
        notesField.clear();

        appointmentsTable.getSelectionModel().clearSelection();
    }

    private void selectPatientById(Long patientId) {
        if (patientId == null) {
            patientComboBox.setValue(null);
            return;
        }

        PatientModel selectedPatient = patientItems.stream()
                .filter(patient -> patient.getId() != null && patient.getId().equals(patientId))
                .findFirst()
                .orElse(null);

        patientComboBox.setValue(selectedPatient);
    }

    private void setDateAndTimeFields(String appointmentDateTime) {
        if (appointmentDateTime == null || appointmentDateTime.isBlank()) {
            appointmentDatePicker.setValue(null);
            appointmentTimeField.clear();
            return;
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(appointmentDateTime);
            appointmentDatePicker.setValue(dateTime.toLocalDate());
            appointmentTimeField.setText(dateTime.toLocalTime().toString().substring(0, 5));
        } catch (Exception e) {
            appointmentDatePicker.setValue(null);
            appointmentTimeField.clear();
        }
    }

    private boolean isValidTime(String time) {
        if (time == null || !time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return false;
        }

        try {
            LocalTime.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

    private record AppointmentFormData(
            Long patientId,
            String appointmentDateTime,
            String status,
            String doctorName,
            String notes
    ) {
    }
}