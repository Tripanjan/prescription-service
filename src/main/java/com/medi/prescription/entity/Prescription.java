package com.medi.prescription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String patientName;
    
    @Column(nullable = false)
    private String doctorName;
    
    @Column(nullable = false)
    private LocalDateTime prescriptionDate;
    
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private Double subtotal;
    
    @Column(nullable = false)
    private Double discountAmount;
    
    @Column(nullable = false)
    private Double discountPercentage;
    
    @Column(nullable = false)
    private Double totalAmount;
    
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        prescriptionDate = LocalDateTime.now();
    }
    
    public void addItem(PrescriptionItem item) {
        items.add(item);
        item.setPrescription(this);
    }
    
    public void removeItem(PrescriptionItem item) {
        items.remove(item);
        item.setPrescription(null);
    }
}
