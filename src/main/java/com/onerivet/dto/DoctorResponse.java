package com.onerivet.dto;

import com.onerivet.model.Specialization;
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
