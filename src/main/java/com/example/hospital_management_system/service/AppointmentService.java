package com.example.hospital_management_system.service;

import com.example.hospital_management_system.dto.AppointmentRequest;
import com.example.hospital_management_system.dto.AppointmentResponse;
import com.example.hospital_management_system.exception.NotFoundException;
import com.example.hospital_management_system.exception.SlotUnavailableException;
import com.example.hospital_management_system.model.Appointment;
import com.example.hospital_management_system.model.AppointmentStatus;
import com.example.hospital_management_system.model.Doctor;
import com.example.hospital_management_system.model.Patient;
import com.example.hospital_management_system.repository.AppointmentRepository;
import com.example.hospital_management_system.repository.DoctorRepository;
import com.example.hospital_management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentResponse book(AppointmentRequest request){
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(()-> new NotFoundException("Patient not found with id: "+ request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(()-> new NotFoundException("Doctor not found with id: "+ request.getDoctorId()));
        boolean slotTaken = appointmentRepository.existsByDoctorIdAndSlotAndStatusNot(
                doctor.getId(), request.getSlot(), AppointmentStatus.CANCELLED);
        if(slotTaken) {
            throw new SlotUnavailableException("Slot " + request.getSlot() + " is not available");
        }
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .slot(request.getSlot())
                .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse cancel(Long id){
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Appointment not found with id: " + id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }
    public AppointmentResponse complete(Long id){
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Appointment not found with id: " + id));
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getDoctorSchedule(Long doctorId){
        doctorRepository.findById(doctorId)
                .orElseThrow(()-> new NotFoundException("Doctor not found with id: " + doctorId));
        return appointmentRepository.findByDoctorIdAndStatusNot(doctorId, AppointmentStatus.CANCELLED).stream().map(this::toResponse).toList();
    }

    private AppointmentResponse toResponse(Appointment appointment){
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getName())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .slot(appointment.getSlot())
                .status(appointment.getStatus())
                .build();
    }
}
