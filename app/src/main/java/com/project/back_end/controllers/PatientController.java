package com.project.back_end.controllers;

import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path:}" + "patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private Service service;

    /**
     * 1. Register a new Patient profile.
     * Validates that the patient record does not already exist before creating.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();
        
        boolean isUnique = service.validatePatient(patient);
        if (!isUnique) {
            response.put("error", "Patient already exists with this email or phone");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        int result = patientService.createPatient(patient);
        if (result == 1) {
            response.put("message", "Patient registered successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 2. Authenticate a Patient.
     * Returns a valid login JWT token upon matching the input credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    /**
     * 3. Fetch specific Patient Profile Details.
     * Authorizes and extracts fields directly linked to an active session token context.
     */
    @GetMapping("/details/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (tokenCheck != null) {
            // Converts token failure format to match standard map output requirements
            Map<String, Object> errorMap = new HashMap<>(tokenCheck.getBody());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
        }
        return patientService.getPatientDetails(token);
    }

    /**
     * 4. Multi-Filter Appointment Fetch routing.
     * Intercepts combinations of past/future status windows along with partial name filters.
     */
    @GetMapping("/appointments/filter/{token}")
    public ResponseEntity<Map<String, Object>> filterAppointments(
            @PathVariable("token") String token,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "doctorName", required = false) String doctorName) {

        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (tokenCheck != null) {
            Map<String, Object> errorMap = new HashMap<>(tokenCheck.getBody());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
        }

        return service.filterPatient(condition, doctorName, token);
    }
}
