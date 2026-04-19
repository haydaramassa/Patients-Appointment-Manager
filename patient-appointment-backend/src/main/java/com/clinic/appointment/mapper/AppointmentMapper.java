package com.clinic.appointment.mapper;

import com.clinic.appointment.dto.AppointmentResponse;
import com.clinic.appointment.entity.Appointment;
import com.clinic.patient.entity.Patient;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment appointment) {
        Patient patient = appointment.getPatient();
        String patientName = patient.getFirstName() + " " + patient.getLastName();

        return new AppointmentResponse(
                appointment.getId(),
                patient.getId(),
                patientName,
                appointment.getAppointmentDateTime(),
                appointment.getStatus(),
                appointment.getDoctorName(),
                appointment.getNotes()
        );
    }
}