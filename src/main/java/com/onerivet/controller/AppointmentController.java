package com.onerivet.controller;

import com.onerivet.dto.AppointmentRequest;
import com.onerivet.dto.AppointmentResponse;
import com.onerivet.dto.PageResponse;
import com.onerivet.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    public ResponseEntity<PageResponse<AppointmentResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "slot", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.getAll(name, status,pageable));
    }

}
