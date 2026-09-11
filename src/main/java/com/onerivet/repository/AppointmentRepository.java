package com.onerivet.repository;

import com.onerivet.model.Appointment;
import com.onerivet.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long>, JpaSpecificationExecutor<Appointment> {
    boolean existsByDoctorIdAndSlotAndStatusNot(
            Long doctorId,
            LocalDateTime slot,
            AppointmentStatus status
    );

    List<Appointment> findByDoctorIdAndStatusNot(Long doctorId, AppointmentStatus status);
}
