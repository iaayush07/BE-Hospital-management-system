package com.onerivet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Positive(message = "Age must be positive")
    private int age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Phone is required")
    private String phone;
}
