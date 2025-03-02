package com.example.complaints.service;

import com.example.complaints.dto.ComplaintCreateRequest;
import com.example.complaints.dto.ComplaintUpdateRequest;
import com.example.complaints.exception.ComplaintNotFoundException;
import com.example.complaints.model.Complaint;
import com.example.complaints.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final CountryLookupService countryLookupService;

    @Transactional
    public Complaint addComplaint(ComplaintCreateRequest request, String clientIp) {
        log.info("Received complaint creation request from IP: {}", clientIp);
        return complaintRepository.findByProductIdAndComplainant(request.productId(), request.complainant())
                .map(existing -> {
                    existing.setCounter(existing.getCounter() + 1);
                    log.info("Duplicate complaint found. Incrementing counter to {}", existing.getCounter());
                    return complaintRepository.save(existing);
                })
                .orElseGet(() -> {
                    String country = countryLookupService.getCountryFromIp(clientIp);
                    Complaint newComplaint = Complaint.builder()
                            .productId(request.productId())
                            .content(request.content())
                            .complainant(request.complainant())
                            .createdDate(LocalDateTime.now())
                            .country(country)
                            .counter(1)
                            .build();
                    log.info("Creating new complaint for product {} from {}", request.productId(), country);
                    return complaintRepository.save(newComplaint);
                });
    }

    @Transactional
    public Complaint updateComplaint(Long id, ComplaintUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint with id " + id + " not found"));
        complaint.setContent(request.content());
        log.info("Updating complaint id {} with new content", id);
        return complaintRepository.save(complaint);
    }

    @Cacheable(value = "complaints")
    public List<Complaint> getAllComplaints() {
        log.info("Retrieving all complaints from database");
        return complaintRepository.findAll();
    }
}
