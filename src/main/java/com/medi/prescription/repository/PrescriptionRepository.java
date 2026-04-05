package com.medi.prescription.repository;

import com.medi.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    
    List<Prescription> findByPatientName(String patientName);
    
    List<Prescription> findByDoctorName(String doctorName);
    
    @Query("SELECT p FROM Prescription p WHERE p.prescriptionDate BETWEEN :startDate AND :endDate")
    List<Prescription> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
