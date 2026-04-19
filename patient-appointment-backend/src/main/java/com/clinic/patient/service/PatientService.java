package com.clinic.patient.service;

import com.clinic.exception.ResourceNotFoundException;
import com.clinic.patient.dto.PatientRequest;
import com.clinic.patient.dto.PatientResponse;
import com.clinic.patient.entity.Patient;
import com.clinic.patient.mapper.PatientMapper;
import com.clinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        return PatientMapper.toResponse(patient);
    }

    public List<PatientResponse> searchPatients(String keyword) {
        return patientRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    public PatientResponse createPatient(PatientRequest request) {
        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        Patient patient = PatientMapper.toEntity(request);
        Patient savedPatient = patientRepository.save(patient);
        return PatientMapper.toResponse(savedPatient);
    }

    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (!patient.getPhone().equals(request.getPhone()) && patientRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        PatientMapper.updateEntity(patient, request);
        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toResponse(updatedPatient);
    }

    public String deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patientRepository.delete(patient);
        return "Patient deleted successfully";
    }
}