package com.medi.prescription.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * PrescriptionItem entity for Prescription Service
 * NOTE: medicine_id is now just a Long reference to Medicine Service, not a foreign key!
 */
@Entity
@Table(name = "prescription_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItem implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnore
    private Prescription prescription;
    
    // CRITICAL CHANGE for Microservices:
    // Instead of @ManyToOne to Medicine entity, we just store the medicine ID
    // Medicine details are fetched from Medicine Service via REST
    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;
    
    @Column(name = "medicine_name", nullable = false)
    private String medicineName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double unitPrice;
    
    @Column(nullable = false)
    private Double totalPrice;
    
    private String dosageInstructions;
}
