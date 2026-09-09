package com.example.hospital_management_system.controller;

import com.example.hospital_management_system.dto.PatientRequest;
import com.example.hospital_management_system.dto.PatientResponse;
import com.example.hospital_management_system.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAll() {
        return ResponseEntity.ok(patientService.getAll());
    }
}
