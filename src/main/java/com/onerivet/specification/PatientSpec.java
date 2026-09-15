package com.onerivet.specification;

import com.onerivet.model.Patient;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpec {

    public static Specification<Patient> hasName(String name){
        return (root, query, criteriaBuilder) ->
                name== null || name.isBlank() ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Patient> hasPhone(String phone){
        return (root, query, criteriaBuilder) ->
                phone == null || phone.isBlank() ? null
                : criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
    }
    public static Specification<Patient> hasGender(String gender){
        return (root, query, criteriaBuilder) ->
                gender == null || gender.isBlank() ? null
                : criteriaBuilder.equal(root.get("gender"),gender );
    }
}
