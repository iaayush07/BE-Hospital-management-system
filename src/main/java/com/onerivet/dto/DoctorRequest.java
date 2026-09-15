package com.onerivet.dto;

import com.onerivet.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Specialization is required")
    private Specialization specialization;

    @NotBlank(message = "Phone is required")
    private String phone;
}
