package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.patient.PatientModel;
import com.clinic.patientappointmentfrontend.service.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PatientsController {

    @FXML
    private Label pageTitleLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField addressField;

    @FXML
    private TextArea notesField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<PatientModel> patientsTable;

    @FXML
    private TableColumn<PatientModel, Long> idColumn;

    @FXML
    private TableColumn<PatientModel, String> firstNameColumn;

    @FXML
    private TableColumn<PatientModel, String> lastNameColumn;

    @FXML
    private TableColumn<PatientModel, String> phoneColumn;

    @FXML
    private TableColumn<PatientModel, String> emailColumn;

    @FXML
    private TableColumn<PatientModel, String> genderColumn;

    @FXML
    private TableColumn<PatientModel, String> birthDateColumn;

    @FXML
    private TableColumn<PatientModel, String> addressColumn;

    @FXML
    private TableColumn<PatientModel, String> notesColumn;

    @FXML
    private TableColumn<PatientModel, Void> actionsColumn;

    private final PatientService patientService = new PatientService();
    private final ObservableList<PatientModel> patientItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    private Long editingPatientId = null;

    @FXML
    public void initialize() {
        genderComboBox.setItems(FXCollections.observableArrayList("MALE", "FEMALE"));
        genderComboBox.setValue("MALE");

        disableFutureBirthDates();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        setupLongTextColumn(addressColumn);
        setupLongTextColumn(notesColumn);
        setupActionsColumn();

        patientsTable.setItems(patientItems);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients(newValue);
        });
    }

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        pageTitleLabel.setText("Patients - " + fullName);
        loadPatients();
    }

    private void disableFutureBirthDates() {
        birthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f7d6d6; -fx-text-fill: #999999;");
                }
            }
        });
    }

    private void setupLongTextColumn(TableColumn<PatientModel, String> column) {
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
                    PatientModel patient = getTableView().getItems().get(getIndex());
                    editPatient(patient);
                });

                deleteButton.setOnAction(event -> {
                    PatientModel patient = getTableView().getItems().get(getIndex());
                    deletePatient(patient);
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
    protected void onCreatePatientClick() {
        clearStatus();

        if (editingPatientId != null) {
            showError("You are editing a patient. Click Clear Form before creating a new patient.");
            return;
        }

        PatientFormData formData = getFormData();

        if (!validatePatientForm(formData)) {
            return;
        }

        boolean created = patientService.createPatient(
                currentBasicAuthToken,
                formData.firstName(),
                formData.lastName(),
                formData.birthDate(),
                formData.gender(),
                formData.phone(),
                formData.email(),
                formData.address(),
                formData.notes()
        );

        if (created) {
            showSuccess("Patient created successfully");
            clearForm();
            loadPatients();
        } else {
            showError("Failed to create patient. Please check the backend response.");
        }
    }

    private void editPatient(PatientModel patient) {
        clearStatus();

        if (patient == null) {
            showError("Please select a patient to edit");
            return;
        }

        editingPatientId = patient.getId();

        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        genderComboBox.setValue(normalizeGender(patient.getGender()));
        addressField.setText(patient.getAddress());
        notesField.setText(patient.getNotes());

        if (patient.getBirthDate() != null && !patient.getBirthDate().isBlank()) {
            try {
                birthDatePicker.setValue(LocalDate.parse(patient.getBirthDate()));
            } catch (Exception e) {
                birthDatePicker.setValue(null);
            }
        } else {
            birthDatePicker.setValue(null);
        }

        showSuccess("Editing patient: " + patient.getFirstName() + " " + patient.getLastName());
    }

    @FXML
    protected void onUpdatePatientClick() {
        clearStatus();

        if (editingPatientId == null) {
            showError("Please click Edit on a patient row first");
            return;
        }

        PatientFormData formData = getFormData();

        if (!validatePatientForm(formData)) {
            return;
        }

        boolean updated = patientService.updatePatient(
                currentBasicAuthToken,
                editingPatientId,
                formData.firstName(),
                formData.lastName(),
                formData.birthDate(),
                formData.gender(),
                formData.phone(),
                formData.email(),
                formData.address(),
                formData.notes()
        );

        if (updated) {
            showSuccess("Patient updated successfully");
            clearForm();
            loadPatients();
        } else {
            showError("Failed to update patient");
        }
    }

    private void deletePatient(PatientModel patient) {
        clearStatus();

        if (patient == null) {
            showError("Please select a patient to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Patient");
        confirmation.setHeaderText("Delete selected patient?");
        confirmation.setContentText(
                "Are you sure you want to delete this patient?\n\n"
                        + "Name: " + patient.getFirstName() + " " + patient.getLastName() + "\n"
                        + "Phone: " + patient.getPhone()
        );

        ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

        if (result != ButtonType.OK) {
            showError("Delete cancelled");
            return;
        }

        boolean deleted = patientService.deletePatient(currentBasicAuthToken, patient.getId());

        if (deleted) {
            showSuccess("Patient deleted successfully");
            clearForm();
            loadPatients();
        } else {
            showError("Failed to delete patient. This patient may have appointments.");
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
        showSuccess("Patients list refreshed");
    }

    @FXML
    protected void onSearchClick() {
        filterPatients(searchField.getText());
    }

    private void filterPatients(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            patientsTable.setItems(patientItems);
            return;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        List<PatientModel> filtered = patientItems.stream()
                .filter(patient ->
                        containsIgnoreCase(patient.getFirstName(), normalizedKeyword)
                                || containsIgnoreCase(patient.getLastName(), normalizedKeyword)
                                || containsIgnoreCase(patient.getPhone(), normalizedKeyword)
                                || containsIgnoreCase(patient.getEmail(), normalizedKeyword)
                                || containsIgnoreCase(patient.getAddress(), normalizedKeyword)
                                || containsIgnoreCase(patient.getNotes(), normalizedKeyword)
                )
                .collect(Collectors.toList());

        patientsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onBackClick() throws Exception {
        SceneManager.loadDashboardScene(currentFullName, currentRole, currentBasicAuthToken);
    }

    private void loadPatients() {
        List<PatientModel> patients = patientService.getAllPatients(currentBasicAuthToken);
        patientItems.setAll(patients);

        if (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) {
            filterPatients(searchField.getText());
        } else {
            patientsTable.setItems(patientItems);
        }
    }

    private PatientFormData getFormData() {
        String firstName = valueOf(firstNameField.getText());
        String lastName = valueOf(lastNameField.getText());
        String phone = valueOf(phoneField.getText());
        String email = valueOf(emailField.getText());
        String gender = valueOf(genderComboBox.getValue());
        String birthDate = birthDatePicker.getValue() == null ? "" : birthDatePicker.getValue().toString();
        String address = valueOf(addressField.getText());
        String notes = valueOf(notesField.getText());

        return new PatientFormData(firstName, lastName, birthDate, gender, phone, email, address, notes);
    }

    private boolean validatePatientForm(PatientFormData formData) {
        if (isBlank(formData.firstName())
                || isBlank(formData.lastName())
                || isBlank(formData.phone())
                || isBlank(formData.gender())
                || isBlank(formData.birthDate())) {
            showError("Please fill first name, last name, phone, gender, and birth date");
            return false;
        }

        if (formData.firstName().length() < 2) {
            showError("First name must be at least 2 characters");
            return false;
        }

        if (formData.lastName().length() < 2) {
            showError("Last name must be at least 2 characters");
            return false;
        }

        if (!isValidPhone(formData.phone())) {
            showError("Please enter a valid phone number");
            return false;
        }

        if (!isBlank(formData.email()) && !isValidEmail(formData.email())) {
            showError("Please enter a valid email address");
            return false;
        }

        if (birthDatePicker.getValue() != null && birthDatePicker.getValue().isAfter(LocalDate.now())) {
            showError("Birth date cannot be in the future");
            return false;
        }

        return true;
    }

    private void clearForm() {
        editingPatientId = null;

        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        emailField.clear();
        genderComboBox.setValue("MALE");
        birthDatePicker.setValue(null);
        addressField.clear();
        notesField.clear();

        patientsTable.getSelectionModel().clearSelection();
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "MALE";
        }

        if (gender.equalsIgnoreCase("Male")) {
            return "MALE";
        }

        if (gender.equalsIgnoreCase("Female")) {
            return "FEMALE";
        }

        return gender;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9+()\\-\\s]{7,20}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String valueOf(String value) {
        return value == null ? "" : value.trim();
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

    private record PatientFormData(
            String firstName,
            String lastName,
            String birthDate,
            String gender,
            String phone,
            String email,
            String address,
            String notes
    ) {
    }
}