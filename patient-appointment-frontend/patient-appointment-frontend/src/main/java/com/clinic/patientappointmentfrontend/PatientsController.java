package com.clinic.patientappointmentfrontend;

import com.clinic.patientappointmentfrontend.patient.PatientModel;
import com.clinic.patientappointmentfrontend.service.PatientService;
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

public class PatientsController {

    @FXML
    private Label pageTitleLabel;

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
    private TableColumn<PatientModel, String> genderColumn;

    @FXML
    private TableColumn<PatientModel, String> birthDateColumn;

    private final PatientService patientService = new PatientService();
    private final ObservableList<PatientModel> patientItems = FXCollections.observableArrayList();

    private String currentFullName;
    private String currentRole;
    private String currentBasicAuthToken;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        patientsTable.setItems(patientItems);
    }

    public void setUserData(String fullName, String role, String basicAuthToken) {
        this.currentFullName = fullName;
        this.currentRole = role;
        this.currentBasicAuthToken = basicAuthToken;

        pageTitleLabel.setText("Patients - " + fullName);
        loadPatients();
    }

    @FXML
    protected void onRefreshClick() {
        loadPatients();
    }

    @FXML
    protected void onSearchClick() {
        String keyword = searchField.getText();

        if (keyword == null || keyword.isBlank()) {
            loadPatients();
            return;
        }

        List<PatientModel> filtered = patientItems.stream()
                .filter(p ->
                        containsIgnoreCase(p.getFirstName(), keyword) ||
                                containsIgnoreCase(p.getLastName(), keyword) ||
                                containsIgnoreCase(p.getPhone(), keyword))
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
        patientsTable.setItems(patientItems);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
}