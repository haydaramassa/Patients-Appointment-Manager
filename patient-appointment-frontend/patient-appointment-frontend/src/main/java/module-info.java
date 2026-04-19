//module com.clinic.patientappointmentfrontend {
//    requires javafx.controls;
//    requires javafx.fxml;
//
//    opens com.clinic.patientappointmentfrontend to javafx.fxml;
//    exports com.clinic.patientappointmentfrontend;
//}


module com.clinic.patientappointmentfrontend {
        requires javafx.controls;
        requires javafx.fxml;
        requires javafx.base;

        opens com.clinic.patientappointmentfrontend to javafx.fxml;
        opens com.clinic.patientappointmentfrontend.user to javafx.base;
        opens com.clinic.patientappointmentfrontend.patient to javafx.base;
        opens com.clinic.patientappointmentfrontend.appointment to javafx.base;

        exports com.clinic.patientappointmentfrontend;
        exports com.clinic.patientappointmentfrontend.user;
        exports com.clinic.patientappointmentfrontend.patient;
        exports com.clinic.patientappointmentfrontend.appointment;
        }