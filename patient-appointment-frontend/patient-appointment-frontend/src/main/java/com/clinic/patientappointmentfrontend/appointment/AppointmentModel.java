package com.clinic.patientappointmentfrontend.appointment;

public class AppointmentModel {

    private Long id;
    private Long patientId;
    private String patientName;
    private String appointmentDateTime;
    private String status;
    private String doctorName;
    private String notes;

    public AppointmentModel() {
    }

    public AppointmentModel(Long id, Long patientId, String patientName,
                            String appointmentDateTime, String status,
                            String doctorName, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.doctorName = doctorName;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getNotes() {
        return notes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}