package com.mediator.common.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard error response returned when an API operation fails")
public class ErrorResponse {

    @Schema(description = "Indicates whether the API operation was successful", example = "false")
    private boolean success;

    @Schema(description = "Description of the error", example = "Validation failed.")
    private String message;

    @Schema(description = "Field-specific validation errors, where applicable", example = "{\"email\":\"Email must be valid\",\"password\":\"Password is required\"}")
    private Map<String, String> errors;

    @Builder.Default
    @Schema(description = "Date and time when the error response was generated", example = "2026-11-11T10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();
}