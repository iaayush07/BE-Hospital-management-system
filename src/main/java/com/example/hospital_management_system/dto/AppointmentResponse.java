package com.example.hospital_management_system.dto;

import com.example.hospital_management_system.model.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime slot;
    private AppointmentStatus status;
}
