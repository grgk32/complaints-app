package com.example.complaints.controller;

import com.example.complaints.dto.ComplaintCreateRequest;
import com.example.complaints.dto.ComplaintResponse;
import com.example.complaints.dto.ComplaintUpdateRequest;
import com.example.complaints.mapper.ComplaintMapper;
import com.example.complaints.model.Complaint;
import com.example.complaints.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints API", description = "Operations for managing complaints")
@Slf4j
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintMapper complaintMapper;

    @Operation(
            summary = "Create a new complaint",
            description = "Creates a complaint. If a complaint with the same product ID and complainant exists, " +
                    "it increments the complaint counter instead of creating a new entry."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse createComplaint(@Valid @RequestBody ComplaintCreateRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("Using client IP: {}", clientIp);
        Complaint complaint = complaintService.addComplaint(request, clientIp);
        return complaintMapper.toResponse(complaint);
    }

    @Operation(
            summary = "Update complaint content",
            description = "Updates the content of an existing complaint identified by its ID."
    )
    @PutMapping("/{id}")
    public ComplaintResponse updateComplaint(@PathVariable Long id, @Valid @RequestBody ComplaintUpdateRequest request) {
        Complaint updated = complaintService.updateComplaint(id, request);
        return complaintMapper.toResponse(updated);
    }

    @Operation(
            summary = "Get all complaints",
            description = "Retrieves a list of all complaints stored in the system."
    )
    @GetMapping
    public List<ComplaintResponse> getAllComplaints() {
        List<Complaint> complaints = complaintService.getAllComplaints();
        return complaints.stream().map(complaintMapper::toResponse).collect(Collectors.toList());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");

        log.info("X-Forwarded-For header: {}", xForwardedFor);
        log.info("X-Real-IP header: {}", xRealIp);

        String ipAddress = null;

        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            ipAddress = xForwardedFor.split(",")[0].trim();
        }

        if ((ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress))
                && xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            ipAddress = xRealIp;
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        log.info("Final extracted IP: {}", ipAddress);
        return ipAddress;
    }

}
