package com.medi.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemResponseDTO {
    
    private Long id;
    private String medicineName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String dosageInstructions;
}
