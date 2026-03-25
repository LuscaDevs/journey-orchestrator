package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.Map;

/**
 * Utility class for creating RFC 9457-compliant ProblemDetail responses
 * from domain exceptions with proper HTTP status mapping and error context.
 */
@Getter
@Builder
public class ErrorResponseProblemDetail {
    private final ProblemDetail problemDetail;
    private final String errorCode;
    private final Instant timestamp;
    private final String path;
    private final Map<String, Object> additionalContext;

    /**
     * Creates an ErrorResponseProblemDetail from a domain exception and HTTP
     * request.
     * 
     * @param ex      The domain exception to convert
     * @param request The HTTP request for context
     * @return ErrorResponseProblemDetail with RFC 9457 compliance
     */
    public static ErrorResponseProblemDetail from(DomainException ex,
            HttpServletRequest request) {
        return ErrorResponseProblemDetail.builder()
                .problemDetail(createProblemDetail(ex))
                .errorCode(ex.getErrorCode().getCode())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .additionalContext(ex.getContext())
                .build();
    }

    /**
     * Creates a ProblemDetail instance following RFC 9457 specification.
     * 
     * @param ex The domain exception
     * @return RFC 9457-compliant ProblemDetail
     */
    private static ProblemDetail createProblemDetail(DomainException ex) {
        HttpStatus httpStatus = getHttpStatusForErrorCode(ex.getErrorCode());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                httpStatus,
                ex.getDetails());

        problemDetail.setTitle(ex.getErrorCode().getDefaultMessage());
        problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/" +
                ex.getErrorCode().getCode().toLowerCase()));
        problemDetail.setInstance(URI.create("urn:uuid:" + java.util.UUID.randomUUID()));

        // Add error code as a custom property
        problemDetail.setProperty("errorCode", ex.getErrorCode().getCode());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /**
     * Maps error codes to appropriate HTTP status codes following REST best
     * practices.
     * 
     * @param errorCode The domain error code
     * @return Corresponding HTTP status
     */
    private static HttpStatus getHttpStatusForErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case JOURNEY_DEFINITION_NOT_FOUND, JOURNEY_INSTANCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_STATE_TRANSITION, JOURNEY_ALREADY_COMPLETED -> HttpStatus.valueOf(422); // Unprocessable Entity
            case CONCURRENT_MODIFICATION, RESOURCE_LOCKED -> HttpStatus.CONFLICT;
            case VALIDATION_FAILED, INVALID_REQUEST_FORMAT, MISSING_REQUIRED_FIELD -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR, DATABASE_ERROR, EXTERNAL_SERVICE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
