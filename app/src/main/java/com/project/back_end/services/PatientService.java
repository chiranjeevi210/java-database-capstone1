package com.project.back_end.services;

import com.project.back_end.models.Patient;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    public int createPatient(Patient patient) {
        try {
            if (patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) != null) {
                return 0; // Guard against duplicated profiles
            }
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        String decodedEmail = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(decodedEmail);

        if (patient == null || !patient.getId().equals(id)) {
            response.put("error", "Access denied. Patient identification token mismatch.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Appointment> appointments = appointmentRepository.findByPatientId(id);
        response.put("appointments", appointments);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        // Status definition maps: 1 specifies historical past logs, 0 tracks upcoming future slots
        int statusCriteria = "past".equalsIgnoreCase(condition) ? 1 : 0;
        
        List<Appointment> matched = appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, statusCriteria);
                
        response.put("appointments", matched);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        List<Appointment> filtered = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
        response.put("appointments", filtered);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        int statusCriteria = "past".equalsIgnoreCase(condition) ? 1 : 0;
        
        List<Appointment> filtered = appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(name, patientId, statusCriteria);
                
        response.put("appointments", filtered);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        String email = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            response.put("error", "Patient identity profile not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("patient", patient);
        return ResponseEntity.ok(response);
    }
}
