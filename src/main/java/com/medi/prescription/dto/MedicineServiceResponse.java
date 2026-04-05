package com.medi.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper from Medicine Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineServiceResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
