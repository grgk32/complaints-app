package com.example.complaints.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintCreateRequest(
        @NotBlank(message = "Product ID is mandatory") String productId,
        @NotBlank(message = "Content is mandatory") String content,
        @NotBlank(message = "Complainant is mandatory") String complainant
) {}
