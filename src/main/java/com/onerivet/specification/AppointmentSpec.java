package com.onerivet.specification;

import com.onerivet.model.Appointment;
import com.onerivet.model.AppointmentStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentSpec {
    public static Specification<Appointment> hasName(String name){
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            query.distinct(true);
            String pattern = "%" + name.toLowerCase() + "%";
            Join<?, ?> patient = root.join("patient", JoinType.LEFT);
            Join<?, ?> doctor = root.join("doctor", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(patient.get("name")), pattern),
                    cb.like(cb.lower(doctor.get("name")), pattern)
            );
        };
    }

    public static Specification<Appointment> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            try {
                return cb.equal(root.get("status"), AppointmentStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                return cb.disjunction();
            }
        };
    }
}
