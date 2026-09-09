package com.example.hospital_management_system.service;

import com.example.hospital_management_system.dto.PatientRequest;
import com.example.hospital_management_system.dto.PatientResponse;
import com.example.hospital_management_system.exception.NotFoundException;
import com.example.hospital_management_system.model.Patient;
import com.example.hospital_management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientResponse register(PatientRequest request){
        Patient patient = Patient.builder()
                .name(request.getName())
                .age(request.getAge())
                .phone(request.getPhone())
                .gender(request.getGender())
                .build();
        return toResponse(patientRepository.save(patient));
    }

    public PatientResponse getById(Long id){
        Patient patient = patientRepository.findById(id).orElseThrow(()-> new NotFoundException("Patient not found with id: " + id));
        return toResponse(patient);
    }

    public List<PatientResponse> getAll() {
        return patientRepository.findAll().stream().map(this::toResponse).toList();
    }

    private PatientResponse toResponse(Patient patient){
        return PatientResponse.builder()
                .id(patient.getId())
                .age(patient.getAge())
                .name(patient.getName())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .build();
    }
}
