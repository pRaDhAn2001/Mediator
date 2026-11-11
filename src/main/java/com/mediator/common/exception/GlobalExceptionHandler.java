package com.mediator.common.exception;

import com.mediator.common.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {

                return ResponseEntity.badRequest()
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message("Invalid email or password.")
                                                .build());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();

                for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                }

                return ResponseEntity.badRequest()
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message("Validation failed.")
                                                .errors(errors)
                                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {

                return ResponseEntity.badRequest()
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        org.springframework.web.HttpRequestMethodNotSupportedException ex) {

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message("HTTP method not supported: " + ex.getMethod())
                                                .build());
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMessageNotReadable(
                        org.springframework.http.converter.HttpMessageNotReadableException ex) {

                return ResponseEntity.badRequest()
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message("Malformed JSON request.")
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.builder()
                                                .success(false)
                                                .message("Something went wrong.")
                                                .build());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex) {

                ErrorResponse response = new ErrorResponse(
                                false,
                                ex.getMessage(),
                                null,
                                LocalDateTime.now());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, String>> handleAccessDeniedException(
                        AccessDeniedException ex) {

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message", ex.getMessage()));
        }
}