package com.example.complaints.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintUpdateRequest(
        @NotBlank(message = "Content is mandatory") String content
) {}
