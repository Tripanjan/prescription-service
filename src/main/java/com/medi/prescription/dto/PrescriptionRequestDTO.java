package com.medi.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequestDTO {
    
    @NotBlank(message = "Patient name is required")
    private String patientName;
    
    @NotBlank(message = "Doctor name is required")
    private String doctorName;
    
    @NotEmpty(message = "At least one medicine item is required")
    @Valid
    private List<PrescriptionItemDTO> items;
    
    private String notes;
}
