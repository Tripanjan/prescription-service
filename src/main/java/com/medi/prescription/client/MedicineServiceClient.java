package com.medi.prescription.client;

import com.medi.prescription.dto.MedicineDTO;
import com.medi.prescription.dto.MedicineServiceResponse;
import com.medi.prescription.dto.StockCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for communicating with Medicine Service
 * 
 * @FeignClient annotation does the following:
 * - name: Logical name of the client (used for service discovery)
 * - url: Base URL of the medicine service (from application.yaml)
 * 
 * Feign automatically:
 * - Creates the implementation at runtime
 * - Handles HTTP requests/responses
 * - Serializes/deserializes JSON
 * - Handles errors and retries (with configuration)
 */
@FeignClient(
    name = "medicine-service",
    url = "${medicine.service.url}"
)
public interface MedicineServiceClient {
    
    /**
     * Get medicine details from Medicine Service
     * 
     * @GetMapping: Maps to HTTP GET request
     * @PathVariable: Extracts {medicineId} from URL path
     * 
     * Feign will call: GET {medicine.service.url}/{medicineId}
     * And automatically deserialize JSON response to MedicineServiceResponse<MedicineDTO>
     */
    @GetMapping("/{medicineId}")
    MedicineServiceResponse<MedicineDTO> getMedicineById(@PathVariable("medicineId") Long medicineId);
    
    /**
     * Check if medicine has sufficient stock
     * 
     * @RequestParam: Adds query parameter to URL
     * 
     * Feign will call: GET {medicine.service.url}/{medicineId}/stock?quantity={quantity}
     * And automatically deserialize JSON response to StockCheckResponse
     */
    @GetMapping("/{medicineId}/stock")
    StockCheckResponse checkStock(
        @PathVariable("medicineId") Long medicineId,
        @RequestParam("quantity") Integer quantity
    );
    
    /**
     * Reduce medicine stock
     * 
     * @PostMapping: Maps to HTTP POST request
     * 
     * Feign will call: POST {medicine.service.url}/{medicineId}/reduce-stock?quantity={quantity}
     * No request body needed (null body)
     */
    @PostMapping("/{medicineId}/reduce-stock")
    void reduceStock(
        @PathVariable("medicineId") Long medicineId,
        @RequestParam("quantity") Integer quantity
    );
}
