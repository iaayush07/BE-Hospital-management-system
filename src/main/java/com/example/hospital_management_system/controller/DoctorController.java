package com.example.hospital_management_system.controller;

import com.example.hospital_management_system.dto.AppointmentResponse;
import com.example.hospital_management_system.dto.DoctorRequest;
import com.example.hospital_management_system.dto.DoctorResponse;
import com.example.hospital_management_system.service.AppointmentService;
import com.example.hospital_management_system.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<DoctorResponse> register(@Valid @RequestBody DoctorRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<AppointmentResponse>> getSchedule(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.getDoctorSchedule(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAll() {
        return ResponseEntity.ok(doctorService.getAll());
    }
}
