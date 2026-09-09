package com.example.hospital_management_system.controller;

import com.example.hospital_management_system.dto.AppointmentRequest;
import com.example.hospital_management_system.dto.AppointmentResponse;
import com.example.hospital_management_system.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody AppointmentRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.complete(id));
    }

}
