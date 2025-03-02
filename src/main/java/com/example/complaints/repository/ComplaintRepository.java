package com.example.complaints.repository;

import com.example.complaints.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByProductIdAndComplainant(String productId, String complainant);
}
