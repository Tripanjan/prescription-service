package com.medi.prescription.service;

import com.medi.prescription.client.MedicineServiceClient;
import com.medi.prescription.dto.*;
import com.medi.prescription.entity.Prescription;
import com.medi.prescription.entity.PrescriptionItem;
import com.medi.prescription.exception.ResourceNotFoundException;
import com.medi.prescription.repository.PrescriptionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Prescription Service for Microservices Architecture
 * CRITICAL CHANGE: Now uses Feign Client instead of RestTemplate for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    
    // MICROSERVICES CHANGE: Inject MedicineServiceClient instead of MedicineRepository
    private final MedicineServiceClient medicineServiceClient;
    
    @Value("${discount.tier1.threshold}")
    private double tier1Threshold;
    
    @Value("${discount.tier1.percentage}")
    private double tier1Percentage;
    
    @Value("${discount.tier2.threshold}")
    private double tier2Threshold;
    
    @Value("${discount.tier2.percentage}")
    private double tier2Percentage;
    
    @Value("${discount.tier3.threshold}")
    private double tier3Threshold;
    
    @Value("${discount.tier3.percentage}")
    private double tier3Percentage;
    
    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        log.info("Fetching prescription with ID: {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        return convertToResponseDTO(prescription);
    }
    
    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        log.info("Fetching all prescriptions");
        return prescriptionRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO) {
        log.info("Creating new prescription for patient: {}", requestDTO.getPatientName());
        
        Prescription prescription = new Prescription();
        prescription.setPatientName(requestDTO.getPatientName());
        prescription.setDoctorName(requestDTO.getDoctorName());
        prescription.setNotes(requestDTO.getNotes());
        
        double subtotal = 0.0;
        
        // Process each prescription item
        for (PrescriptionItemDTO itemDTO : requestDTO.getItems()) {
            
            // FEIGN CLIENT CHANGE #1: Call Medicine Service using Feign Client
            // Instead of manually constructing URLs and parsing responses,
            // Feign handles everything declaratively
            log.info("Fetching medicine details from Medicine Service for ID: {}", itemDTO.getMedicineId());
            MedicineDTO medicine = medicineServiceClient.getMedicineById(itemDTO.getMedicineId()).getData();
            
            // FEIGN CLIENT CHANGE #2: Check stock - Feign automatically deserializes response
            log.info("Checking stock availability for medicine: {}", medicine.getName());
            boolean hasStock = medicineServiceClient.checkStock(medicine.getId(), itemDTO.getQuantity()).isAvailable();
            
            if (!hasStock) {
                throw new IllegalArgumentException("Insufficient stock for medicine: " + medicine.getName() + 
                        ". Requested: " + itemDTO.getQuantity());
            }
            
            // Create prescription item (store medicine ID and name, not entity reference)
            PrescriptionItem item = new PrescriptionItem();
            item.setMedicineId(medicine.getId());  // Store ID only
            item.setMedicineName(medicine.getName());  // Store name for display
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(medicine.getPrice());
            item.setTotalPrice(medicine.getPrice() * itemDTO.getQuantity());
            item.setDosageInstructions(itemDTO.getDosageInstructions());
            
            prescription.addItem(item);
            subtotal += item.getTotalPrice();
            
            // FEIGN CLIENT CHANGE #3: Reduce stock - Feign handles the POST request
            log.info("Reducing stock in Medicine Service for medicine: {}", medicine.getName());
            medicineServiceClient.reduceStock(medicine.getId(), itemDTO.getQuantity());
        }
        
        // Calculate discount based on subtotal (business logic stays the same)
        DiscountInfo discountInfo = calculateDiscount(subtotal);
        
        prescription.setSubtotal(subtotal);
        prescription.setDiscountPercentage(discountInfo.getPercentage());
        prescription.setDiscountAmount(discountInfo.getAmount());
        prescription.setTotalAmount(subtotal - discountInfo.getAmount());
        
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        log.info("Prescription created successfully with ID: {} and total amount: {}", 
                savedPrescription.getId(), savedPrescription.getTotalAmount());
        
        PrescriptionResponseDTO response = convertToResponseDTO(savedPrescription);
        response.setDiscountTier(discountInfo.getTier());
        
        return response;
    }
    
    public List<PrescriptionResponseDTO> getPrescriptionsByPatient(String patientName) {
        log.info("Fetching prescriptions for patient: {}", patientName);
        return prescriptionRepository.findByPatientName(patientName).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public void deletePrescription(Long id) {
        log.info("Deleting prescription with ID: {}", id);
        
        if (!prescriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescription not found with id: " + id);
        }
        
        prescriptionRepository.deleteById(id);
        log.info("Prescription deleted successfully with ID: {}", id);
    }
    
    // Calculate discount based on business rules
    private DiscountInfo calculateDiscount(double subtotal) {
        DiscountInfo discountInfo = new DiscountInfo();
        
        if (subtotal >= tier3Threshold) {
            discountInfo.setPercentage(tier3Percentage);
            discountInfo.setAmount(subtotal * tier3Percentage / 100);
            discountInfo.setTier("Tier 3 - " + tier3Percentage + "% discount (₹" + tier3Threshold + "+)");
        } else if (subtotal >= tier2Threshold) {
            discountInfo.setPercentage(tier2Percentage);
            discountInfo.setAmount(subtotal * tier2Percentage / 100);
            discountInfo.setTier("Tier 2 - " + tier2Percentage + "% discount (₹" + tier2Threshold + "+)");
        } else if (subtotal >= tier1Threshold) {
            discountInfo.setPercentage(tier1Percentage);
            discountInfo.setAmount(subtotal * tier1Percentage / 100);
            discountInfo.setTier("Tier 1 - " + tier1Percentage + "% discount (₹" + tier1Threshold + "+)");
        } else {
            discountInfo.setPercentage(0.0);
            discountInfo.setAmount(0.0);
            discountInfo.setTier("No discount - Subtotal below ₹" + tier1Threshold);
        }
        
        log.info("Applied discount: {}% on subtotal: {}, discount amount: {}", 
                discountInfo.getPercentage(), subtotal, discountInfo.getAmount());
        
        return discountInfo;
    }
    
    // Helper methods
    private PrescriptionResponseDTO convertToResponseDTO(Prescription prescription) {
        List<PrescriptionItemResponseDTO> itemDTOs = prescription.getItems().stream()
                .map(item -> new PrescriptionItemResponseDTO(
                        item.getId(),
                        item.getMedicineName(),  // Uses stored medicine name
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice(),
                        item.getDosageInstructions()
                ))
                .collect(Collectors.toList());
        
        PrescriptionResponseDTO responseDTO = new PrescriptionResponseDTO();
        responseDTO.setId(prescription.getId());
        responseDTO.setPatientName(prescription.getPatientName());
        responseDTO.setDoctorName(prescription.getDoctorName());
        responseDTO.setPrescriptionDate(prescription.getPrescriptionDate());
        responseDTO.setItems(itemDTOs);
        responseDTO.setSubtotal(prescription.getSubtotal());
        responseDTO.setDiscountPercentage(prescription.getDiscountPercentage());
        responseDTO.setDiscountAmount(prescription.getDiscountAmount());
        responseDTO.setTotalAmount(prescription.getTotalAmount());
        responseDTO.setNotes(prescription.getNotes());
        
        return responseDTO;
    }
    
    @Data
    static class DiscountInfo {
        private double percentage;
        private double amount;
        private String tier;
    }
}
