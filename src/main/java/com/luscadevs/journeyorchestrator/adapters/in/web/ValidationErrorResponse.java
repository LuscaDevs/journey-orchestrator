package com.luscadevs.journeyorchestrator.adapters.in.web;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for creating validation error responses with field-level
 * details
 * when request validation fails using Spring's @Valid annotation.
 */
@Getter
@Builder
public class ValidationErrorResponse {
    private final ProblemDetail problemDetail;
    private final String errorCode;
    private final Instant timestamp;
    private final String path;
    private final List<FieldValidationError> fieldErrors;

    /**
     * Creates a ValidationErrorResponse from a MethodArgumentNotValidException and
     * HTTP request.
     * 
     * @param ex      The validation exception containing field errors
     * @param request The HTTP request for context
     * @return ValidationErrorResponse with detailed field validation information
     */
    public static ValidationErrorResponse from(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ValidationErrorResponse::createFieldValidationError)
                .collect(Collectors.toList());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed");

        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/validation_001"));
        problemDetail.setProperty("errorCode", "VALIDATION_001");
        problemDetail.setProperty("fieldCount", fieldErrors.size());

        return ValidationErrorResponse.builder()
                .problemDetail(problemDetail)
                .errorCode("VALIDATION_001")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
    }

    /**
     * Creates a FieldValidationError from Spring's FieldError.
     * 
     * @param fieldError The Spring FieldError to convert
     * @return FieldValidationError with converted information
     */
    private static FieldValidationError createFieldValidationError(FieldError fieldError) {
        return FieldValidationError.builder()
                .field(fieldError.getField())
                .errorCode("VALIDATION_003") // Default validation error code
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
}
