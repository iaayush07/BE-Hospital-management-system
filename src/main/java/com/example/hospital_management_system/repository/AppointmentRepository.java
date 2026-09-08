package com.example.hospital_management_system.repository;

import com.example.hospital_management_system.model.Appointment;
import com.example.hospital_management_system.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    boolean existsByDoctorIdAndSlotAndStatusNot(
            Long doctorId,
            LocalDateTime slot,
            AppointmentStatus status
    );
}
