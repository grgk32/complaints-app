package com.example.complaints.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final int code;
    private final String message;
    private final LocalDateTime timestamp;
}
