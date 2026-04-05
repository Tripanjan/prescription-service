package com.medi.prescription.controller;

import com.medi.prescription.dto.PrescriptionRequestDTO;
import com.medi.prescription.dto.PrescriptionResponseDTO;
import com.medi.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Slf4j
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPrescriptions() {
        log.info("GET /api/prescriptions - Fetching all prescriptions");
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.getAllPrescriptions();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Prescriptions retrieved successfully");
        response.put("data", prescriptions);
        response.put("count", prescriptions.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPrescriptionById(@PathVariable Long id) {
        log.info("GET /api/prescriptions/{} - Fetching prescription by ID", id);
        PrescriptionResponseDTO prescription = prescriptionService.getPrescriptionById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Prescription retrieved successfully");
        response.put("data", prescription);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patient/{patientName}")
    public ResponseEntity<Map<String, Object>> getPrescriptionsByPatient(@PathVariable String patientName) {
        log.info("GET /api/prescriptions/patient/{} - Fetching prescriptions by patient", patientName);
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.getPrescriptionsByPatient(patientName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Patient prescriptions retrieved successfully");
        response.put("data", prescriptions);
        response.put("count", prescriptions.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPrescription(
            @Valid @RequestBody PrescriptionRequestDTO prescriptionRequestDTO) {
        log.info("POST /api/prescriptions - Creating new prescription for patient: {}", 
                prescriptionRequestDTO.getPatientName());
        
        PrescriptionResponseDTO createdPrescription = prescriptionService.createPrescription(prescriptionRequestDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Prescription created successfully with discount applied");
        response.put("data", createdPrescription);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePrescription(@PathVariable Long id) {
        log.info("DELETE /api/prescriptions/{} - Deleting prescription", id);
        prescriptionService.deletePrescription(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Prescription deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}
