package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path:}" + "prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private Service service;

    /**
     * 1. Add a New Medical Prescription Document.
     * Validates that the active session token belongs to an authorized medical Doctor.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addNewPrescription(
            @PathVariable("token") String token,
            @RequestBody Prescription prescription) {

        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "doctor");
        if (tokenCheck != null) {
            return tokenCheck;
        }

        return prescriptionService.savePrescription(prescription);
    }

    /**
     * 2. Fetch Prescription Records linked to an Appointment context.
     * Ensures the requesting user has an active, valid role footprint in the system.
     */
    @GetMapping("/{appointmentId}/{role}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescriptionRecords(
            @PathVariable("appointmentId") Long appointmentId,
            @PathVariable("role") String role,
            @PathVariable("token") String token) {

        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, role);
        if (tokenCheck != null) {
            Map<String, Object> errorMap = new HashMap<>(tokenCheck.getBody());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
