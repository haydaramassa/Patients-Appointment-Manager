package com.clinic.appointment.dto;

import com.clinic.appointment.entity.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentRequest {

    private Long patientId;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String doctorName;
    private String notes;

    public AppointmentRequest() {
    }

    public AppointmentRequest(Long patientId, LocalDateTime appointmentDateTime,
                              AppointmentStatus status, String doctorName, String notes) {
        this.patientId = patientId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.doctorName = doctorName;
        this.notes = notes;
    }

    public Long getPatientId() {
        return patientId;
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

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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