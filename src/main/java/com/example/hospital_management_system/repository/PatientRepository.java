package com.example.hospital_management_system.repository;

import com.example.hospital_management_system.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
