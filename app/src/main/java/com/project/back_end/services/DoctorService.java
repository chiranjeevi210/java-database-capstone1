package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import com.project.back_end.DTO.Login;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * 1. Fetches available time slots for a doctor on a given date by filtering out booked ones.
     */
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        // Define baseline operating slots for the clinic
        List<String> masterSlots = Arrays.asList(
            "09:00-10:00", "10:00-11:00", "11:00-12:00", 
            "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"
        );

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Map booked appointment times to their matching text slot format
        Set<String> bookedSlots = bookedAppointments.stream()
            .map(a -> {
                LocalTime time = a.getAppointmentTime().toLocalTime();
                return String.format("%02d:00-%02d:00", time.getHour(), time.getHour() + 1);
            })
            .collect(Collectors.toSet());

        return masterSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    public int saveDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -1; // Conflict: already exists
        }
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
            return -1; // Not found
        }
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        if (!doctorRepository.existsById(id)) {
            return -1;
        }
        try {
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
        
        if (doctor != null && doctor.getPassword().equals(login.getPassword())) {
            String token = tokenService.generateToken(doctor.getEmail());
            response.put("token", token);
            response.put("userRole", "doctor");
            return ResponseEntity.ok(response);
        }
        
        response.put("error", "Invalid doctor email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 2. Compound Criteria Multi-Filter Mechanics
     */
    public Map<String, Object> filterDoctorsByNameSpecialtyandTime(String name, String specialty, String amorPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors;

        if (name != null && specialty != null) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        } else if (name != null) {
            doctors = doctorRepository.findByNameLike(name);
        } else if (specialty != null) {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        } else {
            doctors = doctorRepository.findAll();
        }

        if (amorPm != null && !amorPm.trim().isEmpty()) {
            doctors = filterDoctorByTime(doctors, amorPm);
        }

        result.put("doctors", doctors);
        return result;
    }

    /**
     * Private runtime utility evaluating string slot configurations against AM/PM boundaries.
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amorPm) {
        return doctors.stream().filter(doc -> {
            // Evaluates doctor available times context elements
            String availability = doc.getAvailability(); 
            if (availability == null) return false;
            
            boolean hasAm = availability.contains("09:00") || availability.contains("10:00") || availability.contains("11:00");
            boolean hasPm = availability.contains("13:00") || availability.contains("14:00") || availability.contains("15:00") || availability.contains("16:00");
            
            if ("AM".equalsIgnoreCase(amorPm)) return hasAm;
            if ("PM".equalsIgnoreCase(amorPm)) return hasPm;
            return true;
        }).collect(Collectors.toList());
    }
}
