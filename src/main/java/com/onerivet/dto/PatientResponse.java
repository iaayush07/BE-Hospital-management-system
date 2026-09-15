package com.onerivet.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatientResponse {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private int age;
}
