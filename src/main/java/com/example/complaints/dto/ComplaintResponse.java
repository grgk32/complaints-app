package com.example.complaints.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        String productId,
        String content,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdDate,
        String complainant,
        String country,
        int counter
) {}
