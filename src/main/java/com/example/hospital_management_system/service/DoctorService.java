package com.example.hospital_management_system.service;

import com.example.hospital_management_system.dto.DoctorRequest;
import com.example.hospital_management_system.dto.DoctorResponse;
import com.example.hospital_management_system.exception.NotFoundException;
import com.example.hospital_management_system.model.Doctor;
import com.example.hospital_management_system.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
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

    public List<DoctorResponse> getAll() {
        return doctorRepository.findAll().stream().map(this::toResponse).toList();
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
