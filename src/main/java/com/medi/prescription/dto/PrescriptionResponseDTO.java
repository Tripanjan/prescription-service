package com.medi.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {
    
    private Long id;
    private String patientName;
    private String doctorName;
    private LocalDateTime prescriptionDate;
    private List<PrescriptionItemResponseDTO> items;
    private Double subtotal;
    private Double discountPercentage;
    private Double discountAmount;
    private Double totalAmount;
    private String notes;
    private String discountTier;
}
