package com.onerivet.specification;

import com.onerivet.model.Doctor;
import com.onerivet.model.Specialization;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class DoctorSpec {
    public static Specification<Doctor> hasName(String name){
        return (root, query, criteriaBuilder) -> name == null || name.isBlank() ? null :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    public static Specification<Doctor> hasSpecialization(String specialization) {
        return (root, query, criteriaBuilder) -> {
            if (specialization == null || specialization.isBlank()) return null;
            try {
                return criteriaBuilder.equal(root.get("specialization"),
                        Specialization.valueOf(specialization.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.disjunction(); // unknown value → return empty
            }
        };
    }
}
