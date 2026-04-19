package com.clinic.dashboard.service;

import com.clinic.appointment.repository.AppointmentRepository;
import com.clinic.dashboard.dto.DashboardSummaryResponse;
import com.clinic.patient.repository.PatientRepository;
import com.clinic.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public DashboardService(PatientRepository patientRepository,
                            AppointmentRepository appointmentRepository,
                            UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long totalPatients = patientRepository.count();
        long totalAppointments = appointmentRepository.count();
        long todayAppointments = appointmentRepository.findByAppointmentDateTimeBetween(start, end).size();
        long totalUsers = userRepository.count();

        return new DashboardSummaryResponse(
                totalPatients,
                totalAppointments,
                todayAppointments,
                totalUsers
        );
    }
}