package com.onerivet.controller;

import com.onerivet.dto.AppointmentResponse;
import com.onerivet.dto.DoctorRequest;
import com.onerivet.dto.DoctorResponse;
import com.onerivet.service.AppointmentService;
import com.onerivet.service.DoctorService;
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
    public ResponseEntity<List<DoctorResponse>> getAll(@RequestParam(required = false) String name,@RequestParam(required = false) String specialization) {
        return ResponseEntity.ok(doctorService.getAll(name, specialization));
    }
}
