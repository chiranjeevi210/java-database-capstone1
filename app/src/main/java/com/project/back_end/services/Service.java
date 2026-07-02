package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Doctor;
import com.project.back_end.DTO.Login;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    /**
     * 1. Dynamic Boundary Gatekeeper: Evaluates authentication tokens against assigned user roles.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String userRole) {
        Map<String, String> errorResponse = new HashMap<>();
        boolean isValid = tokenService.validateToken(token, userRole);
        
        if (!isValid) {
            errorResponse.put("error", "Session token is invalid or has expired.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        return null; // Return null to signal to controllers that validation passed successfully
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (admin != null && admin.getPassword().equals(receivedAdmin.getPassword())) {
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("userRole", "admin");
            return ResponseEntity.ok(response);
        }

        response.put("error", "Invalid admin credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        Patient patient = patientRepository.findByEmail(login.getIdentifier());

        if (patient != null && patient.getPassword().equals(login.getPassword())) {
            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("userRole", "loggedPatient");
            return ResponseEntity.ok(response);
        }

        response.put("error", "Invalid patient email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 2. Live Conflict Validator Checking Schedule Conflicts
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();
        if (!doctorRepository.existsById(doctorId)) {
            return -1; // Doctor record missing entirely
        }

        LocalDate appDate = appointment.getAppointmentTime().toLocalDate();
        LocalTime appTime = appointment.getAppointmentTime().toLocalTime();
        String formattedSlot = String.format("%02d:00-%02d:00", appTime.getHour(), appTime.getHour() + 1);

        // Fetch free slots on that specific calendar day
        List<String> openSlots = doctorService.getDoctorAvailability(doctorId, appDate);

        if (openSlots.contains(formattedSlot)) {
            return 1; // Valid and available
        } else {
            return 0; // Slot unavailable or already taken
        }
    }

    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null; // Returns true if no duplicates exist, allowing insertion
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        return doctorService.filterDoctorsByNameSpecialtyandTime(name, specialty, time);
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        String email = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(email);
        
        if (patient == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Patient session context not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Long pId = patient.getId();
        boolean hasCond = (condition != null && !condition.trim().isEmpty());
        boolean hasName = (name != null && !name.trim().isEmpty());

        if (hasCond && hasName) {
            return patientService.filterByDoctorAndCondition(condition, name, pId);
        } else if (hasCond) {
            return patientService.filterByCondition(condition, pId);
        } else if (hasName) {
            return patientService.filterByDoctor(name, pId);
        } else {
            return patientService.getPatientAppointment(pId, token);
        }
    }
}
