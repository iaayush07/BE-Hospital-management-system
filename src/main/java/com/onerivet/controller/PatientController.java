package com.onerivet.controller;

import com.onerivet.dto.PatientRequest;
import com.onerivet.dto.PatientResponse;
import com.onerivet.service.PatientService;
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
    public ResponseEntity<List<PatientResponse>> getAll(@RequestParam(required = false) String name,@RequestParam(required = false) String phone,@RequestParam(required = false) String gender) {
        return ResponseEntity.ok(patientService.getAll(name, phone, gender));
    }
}
