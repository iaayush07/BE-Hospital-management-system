package com.onerivet.service;

import com.onerivet.dto.DoctorRequest;
import com.onerivet.dto.DoctorResponse;
import com.onerivet.exception.NotFoundException;
import com.onerivet.model.Doctor;
import com.onerivet.repository.DoctorRepository;
import com.onerivet.specification.DoctorSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorResponse register(DoctorRequest request){
        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .build();
        return toResponse((doctorRepository.save(doctor)));
    }

    public DoctorResponse getById(Long id){
        Doctor doctor = doctorRepository.findById(id).orElseThrow(()-> new NotFoundException("Doctor not found with id: " + id));
        return toResponse(doctor);
    }

    public List<DoctorResponse> getAll(String name, String specialization) {
        Specification<Doctor> spec = Specification.where(DoctorSpec.hasName(name).and(DoctorSpec.hasSpecialization(specialization)));
        return doctorRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    private DoctorResponse toResponse(Doctor doctor){
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .phone(doctor.getPhone())
                .specialization(doctor.getSpecialization())
                .build();
    }
}
