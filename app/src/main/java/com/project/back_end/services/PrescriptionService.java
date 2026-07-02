package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    /**
     * Persists an inbound prescription transaction directly to your MongoDB layer.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        try {
            if (prescription == null) {
                response.put("error", "Prescription data object cannot be empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("error", "An internal storage engine fault occurred while processing the script");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Extracts and retrieves custom prescription documents matched to an appointment ID.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (appointmentId == null) {
                response.put("error", "Valid relational identification target required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            List<Prescription> scripts = prescriptionRepository.findByAppointmentId(appointmentId);
            response.put("prescriptions", scripts);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to retrieve matching prescription records from the database");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
