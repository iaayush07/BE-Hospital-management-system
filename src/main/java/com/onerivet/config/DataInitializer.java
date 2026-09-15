package com.onerivet.config;

import com.onerivet.model.*;
import com.onerivet.repository.AppointmentRepository;
import com.onerivet.repository.DoctorRepository;
import com.onerivet.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            PatientRepository patientRepo,
            DoctorRepository doctorRepo,
            AppointmentRepository appointmentRepo
    ) {
        return args -> {
            List<Patient> patients;
            List<Doctor> doctors;

            if (patientRepo.count() == 0) {
                patients = patientRepo.saveAll(List.of(
                        Patient.builder().name("Aarav Sharma").age(34).gender("Male").phone("9876543210").build(),
                        Patient.builder().name("Priya Mehta").age(28).gender("Female").phone("9876543211").build(),
                        Patient.builder().name("Rohan Gupta").age(52).gender("Male").phone("9876543212").build(),
                        Patient.builder().name("Sneha Patel").age(19).gender("Female").phone("9876543213").build(),
                        Patient.builder().name("Vikram Singh").age(45).gender("Male").phone("9876543214").build(),
                        Patient.builder().name("Ananya Joshi").age(31).gender("Female").phone("9876543215").build()
                ));
                doctors = doctorRepo.saveAll(List.of(
                        Doctor.builder().name("Dr. Ramesh Verma").specialization(Specialization.CARDIOLOGY).phone("9911223344").build(),
                        Doctor.builder().name("Dr. Sunita Rao").specialization(Specialization.NEUROLOGY).phone("9911223345").build(),
                        Doctor.builder().name("Dr. Anil Khanna").specialization(Specialization.ORTHOPEDICS).phone("9911223346").build(),
                        Doctor.builder().name("Dr. Kavya Nair").specialization(Specialization.PEDIATRICS).phone("9911223347").build(),
                        Doctor.builder().name("Dr. Deepak Mishra").specialization(Specialization.DERMATOLOGY).phone("9911223348").build()
                ));
            } else {
                patients = patientRepo.findAll();
                doctors = doctorRepo.findAll();
            }

            LocalDateTime now = LocalDateTime.now();

            if (appointmentRepo.count() == 0) {
                appointmentRepo.saveAll(List.of(
                        Appointment.builder().patient(patients.get(0)).doctor(doctors.get(0)).slot(now.plusDays(1).withHour(10).withMinute(0)).build(),
                        Appointment.builder().patient(patients.get(1)).doctor(doctors.get(1)).slot(now.plusDays(2).withHour(11).withMinute(30)).build(),
                        Appointment.builder().patient(patients.get(2)).doctor(doctors.get(2)).slot(now.plusDays(3).withHour(9).withMinute(0)).build(),
                        Appointment.builder().patient(patients.get(3)).doctor(doctors.get(3)).slot(now.plusDays(1).withHour(14).withMinute(0)).build(),
                        Appointment.builder().patient(patients.get(4)).doctor(doctors.get(4)).slot(now.plusDays(4).withHour(16).withMinute(0)).build(),
                        Appointment.builder().patient(patients.get(5)).doctor(doctors.get(0)).slot(now.minusDays(2).withHour(10).withMinute(0)).status(AppointmentStatus.COMPLETED).build(),
                        Appointment.builder().patient(patients.get(0)).doctor(doctors.get(2)).slot(now.minusDays(1).withHour(11).withMinute(0)).status(AppointmentStatus.CANCELLED).build(),
                        Appointment.builder().patient(patients.get(2)).doctor(doctors.get(1)).slot(now.plusDays(5).withHour(15).withMinute(30)).build()
                ));
            }

            if (appointmentRepo.count() >= 31) return;

            appointmentRepo.saveAll(List.of(
                    // --- SCHEDULED ---
                    Appointment.builder().patient(patients.get(5)).doctor(doctors.get(2)).slot(now.plusDays(6).withHour(10).withMinute(30)).build(),
                    Appointment.builder().patient(patients.get(0)).doctor(doctors.get(3)).slot(now.plusDays(7).withHour(12).withMinute(0)).build(),
                    Appointment.builder().patient(patients.get(1)).doctor(doctors.get(4)).slot(now.plusDays(8).withHour(9).withMinute(30)).build(),
                    Appointment.builder().patient(patients.get(3)).doctor(doctors.get(0)).slot(now.plusDays(9).withHour(17).withMinute(0)).build(),
                    Appointment.builder().patient(patients.get(4)).doctor(doctors.get(1)).slot(now.plusDays(10).withHour(11).withMinute(0)).build(),
                    Appointment.builder().patient(patients.get(5)).doctor(doctors.get(3)).slot(now.plusDays(11).withHour(14).withMinute(30)).build(),
                    Appointment.builder().patient(patients.get(2)).doctor(doctors.get(4)).slot(now.plusDays(12).withHour(16).withMinute(0)).build(),

                    // --- COMPLETED ---
                    Appointment.builder().patient(patients.get(1)).doctor(doctors.get(2)).slot(now.minusDays(3).withHour(9).withMinute(0)).status(AppointmentStatus.COMPLETED).build(),
                    Appointment.builder().patient(patients.get(3)).doctor(doctors.get(4)).slot(now.minusDays(4).withHour(11).withMinute(30)).status(AppointmentStatus.COMPLETED).build(),
                    Appointment.builder().patient(patients.get(0)).doctor(doctors.get(1)).slot(now.minusDays(5).withHour(16).withMinute(0)).status(AppointmentStatus.COMPLETED).build(),
                    Appointment.builder().patient(patients.get(4)).doctor(doctors.get(3)).slot(now.minusDays(6).withHour(10).withMinute(0)).status(AppointmentStatus.COMPLETED).build(),
                    Appointment.builder().patient(patients.get(2)).doctor(doctors.get(0)).slot(now.minusDays(7).withHour(14).withMinute(0)).status(AppointmentStatus.COMPLETED).build(),
                    Appointment.builder().patient(patients.get(5)).doctor(doctors.get(4)).slot(now.minusDays(8).withHour(9).withMinute(30)).status(AppointmentStatus.COMPLETED).build(),

                    // --- CANCELLED ---
                    Appointment.builder().patient(patients.get(2)).doctor(doctors.get(3)).slot(now.minusDays(3).withHour(15).withMinute(0)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(4)).doctor(doctors.get(0)).slot(now.minusDays(5).withHour(12).withMinute(30)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(1)).doctor(doctors.get(4)).slot(now.plusDays(2).withHour(16).withMinute(0)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(3)).doctor(doctors.get(1)).slot(now.plusDays(3).withHour(10).withMinute(30)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(5)).doctor(doctors.get(2)).slot(now.minusDays(10).withHour(8).withMinute(30)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(0)).doctor(doctors.get(4)).slot(now.minusDays(12).withHour(17).withMinute(0)).status(AppointmentStatus.CANCELLED).build(),
                    Appointment.builder().patient(patients.get(2)).doctor(doctors.get(0)).slot(now.plusDays(5).withHour(8).withMinute(0)).status(AppointmentStatus.CANCELLED).build()
            ));
        };
    }
}
