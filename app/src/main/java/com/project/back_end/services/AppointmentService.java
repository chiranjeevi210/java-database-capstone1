package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TokenService tokenService;

    // Direct circular injection mitigation through lazy-lookup utility
    @Autowired
    private com.project.back_end.services.Service centralValidationService;

    /**
     * 1. Book a new appointment entry.
     */
    public int bookAppointment(Appointment appointment) {
        try {
            if (appointment == null) return 0;
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0; // Return 0 to gracefully signal a failure back to the controller
        }
    }

    /**
     * 2. Update details on an existing scheduled clinical appointment.
     */
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            response.put("error", "Invalid appointment schema payload");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check if the target allocation entity footprint is present
        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (!existingOpt.isPresent()) {
            response.put("error", "Appointment entry not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Cross-examine configuration conflicts utilizing the central engine state checker
        int validateResult = centralValidationService.validateAppointment(appointment);
        if (validateResult == 0) {
            response.put("error", "Requested appointment slot is already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (validateResult == -1) {
            response.put("error", "Doctor referenced does not exist inside our records");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        appointmentRepository.save(appointment);
        response.put("message", "Appointment updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * 3. Cancel an existing appointment slot.
     */
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (!appointmentOpt.isPresent()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Appointment appointment = appointmentOpt.get();

        // Security Assertion: Ensure that the patient executing this cancellation is the owner
        String callerEmail = tokenService.extractIdentifier(token);
        Patient patient = appointment.getPatient();

        if (patient == null || !patient.getEmail().equalsIgnoreCase(callerEmail)) {
            response.put("error", "Unauthorized operation sequence detected. Possession mismatch.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        appointmentRepository.delete(appointment);
        response.put("message", "Appointment canceled successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * 4. Retrieves appointments scheduled for a specific doctor on a selected date.
     */
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();

        // Resolve doctor reference identity using incoming caller claim strings
        String doctorEmail = tokenService.extractIdentifier(token);
        Doctor doctor = doctorRepository.findByEmail(doctorEmail);

        if (doctor == null) {
            result.put("error", "Doctor matching session credentials not found");
            return result;
        }

        // Establish strict bounds tracking the 24-hour timeframe for the query
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Fetch associated structural entries from the repository layer
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), startOfDay, endOfDay);

        // Filter the output collection if a specific patient name pattern is provided
        if (pname != null && !pname.trim().isEmpty()) {
            appointments = appointments.stream()
                    .filter(a -> a.getPatient() != null && 
                            a.getPatient().getName().toLowerCase().contains(pname.toLowerCase()))
                    .collect(Collectors.toList());
        }

        result.put("appointments", appointments);
        return result;
    }
}
