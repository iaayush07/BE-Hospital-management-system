package com.onerivet.service;

import com.onerivet.dto.PatientRequest;
import com.onerivet.dto.PatientResponse;
import com.onerivet.exception.NotFoundException;
import com.onerivet.model.Patient;
import com.onerivet.repository.PatientRepository;
import com.onerivet.specification.PatientSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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

    public List<PatientResponse> getAll(String name, String phone, String gender) {
        Specification<Patient> spec = Specification.where(PatientSpec.hasName(name)).and(PatientSpec.hasPhone(phone).and(PatientSpec.hasGender(gender)));
        return patientRepository.findAll(spec).stream().map(this::toResponse).toList();
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
