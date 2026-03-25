package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionAlreadyExistsException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyInstanceNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.InvalidStateTransitionException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyAlreadyCompletedException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

/**
 * Global exception handler for centralized error processing across all REST
 * controllers.
 * Provides RFC 9457-compliant error responses with proper HTTP status mapping.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
        private static final String CORRELATION_ID_MDC_KEY = "correlationId";

        /**
         * Handles all domain exceptions and converts them to RFC 9457-compliant
         * responses.
         * 
         * @param ex      The domain exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and appropriate HTTP status
         */
        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ProblemDetail> handleDomainException(
                        DomainException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                // Log with structured context for debugging and monitoring
                log.warn("Domain exception occurred: {} - {} | Path: {} | Context: {} | CorrelationId: {}",
                                ex.getErrorCode().getCode(),
                                ex.getDetails(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId,
                                ex);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(errorResponse.getProblemDetail().getStatus())
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles validation errors from @Valid annotated request parameters.
         * 
         * @param ex      The validation exception containing field errors
         * @param request The HTTP request for context
         * @return ResponseEntity with detailed validation error information
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ProblemDetail> handleValidationException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, null, request);

                log.warn("Validation failed for request {}: {} field errors | CorrelationId: {}",
                                request.getRequestURI(),
                                ex.getBindingResult().getFieldErrors().size(),
                                correlationId);

                ValidationErrorResponse errorResponse = ValidationErrorResponse.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles JourneyDefinitionNotFoundException with specific logging.
         * 
         * @param ex      The exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and HTTP 404 status
         */
        @ExceptionHandler(JourneyDefinitionNotFoundException.class)
        public ResponseEntity<ProblemDetail> handleJourneyDefinitionNotFoundException(
                        JourneyDefinitionNotFoundException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                log.info("Journey definition not found: {} | Path: {} | Requester context: {} | CorrelationId: {}",
                                ex.getJourneyDefinitionId(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles JourneyDefinitionAlreadyExistsException with specific logging.
         * 
         * @param ex      The exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and HTTP 409 status
         */
        @ExceptionHandler(JourneyDefinitionAlreadyExistsException.class)
        public ResponseEntity<ProblemDetail> handleJourneyDefinitionAlreadyExistsException(
                        JourneyDefinitionAlreadyExistsException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                log.warn("Journey definition already exists: {} | Path: {} | Requester context: {} | CorrelationId: {}",
                                ex.getJourneyDefinitionId(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles JourneyInstanceNotFoundException with specific logging.
         * 
         * @param ex      The exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and HTTP 404 status
         */
        @ExceptionHandler(JourneyInstanceNotFoundException.class)
        public ResponseEntity<ProblemDetail> handleJourneyInstanceNotFoundException(
                        JourneyInstanceNotFoundException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                log.info("Journey instance not found: {} | Path: {} | Requester context: {} | CorrelationId: {}",
                                ex.getJourneyInstanceId(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles InvalidStateTransitionException with specific logging.
         * 
         * @param ex      The exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and HTTP 422 status
         */
        @ExceptionHandler(InvalidStateTransitionException.class)
        public ResponseEntity<ProblemDetail> handleInvalidStateTransitionException(
                        InvalidStateTransitionException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                log.warn("Invalid state transition attempted: {} -> {} for instance {} | Path: {} | Context: {} | CorrelationId: {}",
                                ex.getFromState(),
                                ex.getToState(),
                                ex.getJourneyInstanceId(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.valueOf(422))
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles JourneyAlreadyCompletedException with specific logging.
         * 
         * @param ex      The exception that occurred
         * @param request The HTTP request for context
         * @return ResponseEntity with ProblemDetail and HTTP 422 status
         */
        @ExceptionHandler(JourneyAlreadyCompletedException.class)
        public ResponseEntity<ProblemDetail> handleJourneyAlreadyCompletedException(
                        JourneyAlreadyCompletedException ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, ex, request);

                log.warn("Attempted operation on completed journey instance: {} | Path: {} | Context: {} | CorrelationId: {}",
                                ex.getJourneyInstanceId(),
                                request.getRequestURI(),
                                sanitizeContext(ex.getContext()),
                                correlationId);

                ErrorResponseProblemDetail errorResponse = ErrorResponseProblemDetail.from(ex, request);

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.valueOf(422))
                                .body(errorResponse.getProblemDetail());
        }

        /**
         * Handles unexpected exceptions and returns a generic error response.
         * 
         * @param ex      The unexpected exception
         * @param request The HTTP request for context
         * @return ResponseEntity with generic error information
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ProblemDetail> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                String correlationId = getOrCreateCorrelationId(request);
                setupLoggingContext(correlationId, null, request);

                log.error("Unexpected error occurred processing request {} | CorrelationId: {}",
                                request.getRequestURI(),
                                correlationId,
                                ex);

                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred while processing your request");

                problemDetail.setTitle("Internal Server Error");
                problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/system_001"));
                problemDetail.setProperty("errorCode", "SYSTEM_001");
                problemDetail.setProperty("timestamp", java.time.Instant.now().toString());
                problemDetail.setProperty("path", request.getRequestURI());

                clearLoggingContext();

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(problemDetail);
        }

        // === Helper Methods for Structured Logging ===

        /**
         * Gets or creates a correlation ID for request tracking.
         */
        private String getOrCreateCorrelationId(HttpServletRequest request) {
                String correlationId = request.getHeader(CORRELATION_ID_HEADER);
                if (correlationId == null || correlationId.trim().isEmpty()) {
                        correlationId = UUID.randomUUID().toString();
                }
                return correlationId;
        }

        /**
         * Sets up logging context with correlation ID and request information.
         */
        private void setupLoggingContext(String correlationId, DomainException ex, HttpServletRequest request) {
                MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
                MDC.put("requestPath", request.getRequestURI());
                MDC.put("httpMethod", request.getMethod());

                if (ex != null) {
                        MDC.put("errorCode", ex.getErrorCode().getCode());
                        MDC.put("exceptionType", ex.getClass().getSimpleName());
                }
        }

        /**
         * Clears logging context to prevent memory leaks.
         */
        private void clearLoggingContext() {
                MDC.clear();
        }

        /**
         * Sanitizes context to prevent sensitive information exposure.
         */
        private String sanitizeContext(java.util.Map<String, Object> context) {
                if (context == null || context.isEmpty()) {
                        return "none";
                }

                // Create a safe copy of context
                java.util.Map<String, Object> safeContext = new java.util.HashMap<>();

                for (java.util.Map.Entry<String, Object> entry : context.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        // Skip potentially sensitive keys
                        if (isSensitiveKey(key)) {
                                safeContext.put(key, "[REDACTED]");
                        } else {
                                safeContext.put(key, sanitizeValue(value));
                        }
                }

                return safeContext.toString();
        }

        /**
         * Checks if a key might contain sensitive information.
         */
        private boolean isSensitiveKey(String key) {
                if (key == null)
                        return false;
                String lowerKey = key.toLowerCase();
                return lowerKey.contains("password") ||
                                lowerKey.contains("token") ||
                                lowerKey.contains("secret") ||
                                lowerKey.contains("key") ||
                                lowerKey.contains("credential") ||
                                lowerKey.contains("auth");
        }

        /**
         * Sanitizes a potentially sensitive value.
         */
        private Object sanitizeValue(Object value) {
                if (value == null) {
                        return null;
                }

                String strValue = value.toString();

                // If value looks like it might be sensitive, truncate it
                if (strValue.length() > 100) {
                        return strValue.substring(0, 100) + "...[TRUNCATED]";
                }

                return value;
        }
}
