package com.medi.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stock availability response from Medicine Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {
    private boolean available;
    private Integer currentStock;
    private Integer requestedQuantity;
}
