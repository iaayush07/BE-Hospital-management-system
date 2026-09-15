package com.onerivet.service;

import com.onerivet.dto.AppointmentRequest;
import com.onerivet.dto.AppointmentResponse;
import com.onerivet.dto.PageResponse;
import com.onerivet.exception.NotFoundException;
import com.onerivet.exception.SlotUnavailableException;
import com.onerivet.model.Appointment;
import com.onerivet.model.AppointmentStatus;
import com.onerivet.model.Doctor;
import com.onerivet.model.Patient;
import com.onerivet.repository.AppointmentRepository;
import com.onerivet.repository.DoctorRepository;
import com.onerivet.repository.PatientRepository;
import com.onerivet.specification.AppointmentSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
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

    public PageResponse<AppointmentResponse> getAll(String name, String status, Pageable pageable) {
        Specification<Appointment> spec = Specification
                .where(AppointmentSpec.hasName(name))
                .and(AppointmentSpec.hasStatus(status));
        Page<AppointmentResponse> page = appointmentRepository
                .findAll(spec, pageable)
                .map(this::toResponse);
        return PageResponse.from(page);
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
