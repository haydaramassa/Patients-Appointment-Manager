package com.clinic.appointment.dto;

import com.clinic.appointment.entity.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String doctorName;
    private String notes;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, Long patientId, String patientName,
                               LocalDateTime appointmentDateTime, AppointmentStatus status,
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

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
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

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}