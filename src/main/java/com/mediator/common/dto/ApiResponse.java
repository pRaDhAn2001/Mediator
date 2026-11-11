package com.mediator.common.dto;

import lombok.*;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the API operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Data fetched successfully.")
    private String message;

    @Schema(description = "Response payload. Its structure depends on the API endpoint.")
    private T data;

    @Builder.Default
    @Schema(description = "Date and time when the response was generated", example = "2026-11-11T10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}