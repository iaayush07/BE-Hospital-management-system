package com.example.hospital_management_system.dto;

import com.example.hospital_management_system.model.Specialization;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DoctorResponse {
    private Long id;
    private String name;
    private Specialization specialization;
    private String phone;
}
