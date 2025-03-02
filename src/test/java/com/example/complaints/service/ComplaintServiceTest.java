package com.example.complaints.service;

import com.example.complaints.dto.ComplaintCreateRequest;
import com.example.complaints.dto.ComplaintUpdateRequest;
import com.example.complaints.exception.ComplaintNotFoundException;
import com.example.complaints.model.Complaint;
import com.example.complaints.repository.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private CountryLookupService countryLookupService;

    @InjectMocks
    private ComplaintService complaintService;

    private final String ip = "127.0.0.1";

    @BeforeEach
    public void setup() { }

    @Test
    public void testAddComplaint_NewComplaint() {
        ComplaintCreateRequest request = new ComplaintCreateRequest("prod123", "Content of complaint", "John Doe");
        when(complaintRepository.findByProductIdAndComplainant(request.productId(), request.complainant()))
                .thenReturn(Optional.empty());
        when(countryLookupService.getCountryFromIp(ip)).thenReturn("Poland");
        Complaint savedComplaint = Complaint.builder()
                .id(1L)
                .productId(request.productId())
                .content(request.content())
                .complainant(request.complainant())
                .createdDate(LocalDateTime.now())
                .country("Poland")
                .counter(1)
                .build();
        when(complaintRepository.save(any(Complaint.class))).thenReturn(savedComplaint);
        Complaint result = complaintService.addComplaint(request, ip);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getCounter());
        verify(complaintRepository, times(1)).save(any(Complaint.class));
        verify(countryLookupService, times(1)).getCountryFromIp(ip);
    }

    @Test
    public void testAddComplaint_DuplicateComplaint() {
        ComplaintCreateRequest request = new ComplaintCreateRequest("prod123", "Original content", "John Doe");
        Complaint existingComplaint = Complaint.builder()
                .id(1L)
                .productId(request.productId())
                .content(request.content())
                .complainant(request.complainant())
                .createdDate(LocalDateTime.now())
                .country("Poland")
                .counter(1)
                .build();
        when(complaintRepository.findByProductIdAndComplainant(request.productId(), request.complainant()))
                .thenReturn(Optional.of(existingComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(existingComplaint);
        Complaint result = complaintService.addComplaint(request, ip);
        assertNotNull(result);
        assertEquals(2, result.getCounter());
        verify(complaintRepository, times(1)).save(existingComplaint);
        verify(countryLookupService, never()).getCountryFromIp(anyString());
    }

    @Test
    public void testUpdateComplaint_Success() {
        Long complaintId = 1L;
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest("Updated content");
        Complaint existingComplaint = Complaint.builder()
                .id(complaintId)
                .productId("prod123")
                .content("Old content")
                .complainant("John Doe")
                .createdDate(LocalDateTime.now())
                .country("Poland")
                .counter(1)
                .build();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(existingComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(existingComplaint);
        Complaint updated = complaintService.updateComplaint(complaintId, updateRequest);
        assertEquals("Updated content", updated.getContent());
        verify(complaintRepository, times(1)).findById(complaintId);
        verify(complaintRepository, times(1)).save(existingComplaint);
    }

    @Test
    public void testUpdateComplaint_NotFound() {
        Long complaintId = 1L;
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest("Updated content");
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.empty());
        assertThrows(ComplaintNotFoundException.class, () -> complaintService.updateComplaint(complaintId, updateRequest));
        verify(complaintRepository, times(1)).findById(complaintId);
        verify(complaintRepository, never()).save(any());
    }
}
