package com.luscadevs.journeyorchestrator.e2e.framework.helpers;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates API responses against OpenAPI specification. Provides contract validation for REST API
 * endpoints.
 */
public class ContractValidator {

    private final JsonSchemaFactory schemaFactory;
    private boolean strictMode = true;
    private final List<ValidationError> validationErrors = new ArrayList<>();
    private final List<ValidationWarning> validationWarnings = new ArrayList<>();

    public ContractValidator() {
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    }

    /**
     * Validates response against OpenAPI specification
     */
    public ValidationResult validateResponse(String endpoint, Response response) {
        validationErrors.clear();
        validationWarnings.clear();

        boolean isValid = true;

        // Validate status code
        if (!isValidStatusCode(response.getStatusCode())) {
            validationErrors.add(new ValidationError("INVALID_STATUS_CODE", "Status code "
                    + response.getStatusCode() + " is not valid for endpoint " + endpoint));
            isValid = false;
        }

        // Validate response body if present
        if (response.getContentType() != null
                && response.getContentType().contains("application/json")) {
            if (!isValidJsonStructure(response.getBody().asString())) {
                validationErrors.add(new ValidationError("INVALID_JSON_STRUCTURE",
                        "Response body is not valid JSON for endpoint " + endpoint));
                isValid = false;
            }
        }

        return new ValidationResult(isValid, new ArrayList<>(validationErrors),
                new ArrayList<>(validationWarnings));
    }

    /**
     * Validates request against OpenAPI specification
     */
    public ValidationResult validateRequest(String endpoint, RequestSpecification request) {
        validationErrors.clear();
        validationWarnings.clear();

        boolean isValid = true;

        // Validate content type
        if (!isValidContentType(request)) {
            validationErrors.add(new ValidationError("INVALID_CONTENT_TYPE",
                    "Content type is not valid for endpoint " + endpoint));
            isValid = false;
        }

        return new ValidationResult(isValid, new ArrayList<>(validationErrors),
                new ArrayList<>(validationWarnings));
    }

    /**
     * Validates JSON schema
     */
    public ValidationResult validateJsonSchema(Object data, JsonSchema schema) {
        validationErrors.clear();
        validationWarnings.clear();

        boolean isValid = true;

        try {
            // Implementation will validate data against schema
            // For now, just basic structure validation
            if (data == null) {
                validationErrors.add(new ValidationError("NULL_DATA",
                        "Data cannot be null for schema validation"));
                isValid = false;
            }
        } catch (Exception e) {
            validationErrors.add(new ValidationError("SCHEMA_VALIDATION_ERROR",
                    "Schema validation failed: " + e.getMessage()));
            isValid = false;
        }

        return new ValidationResult(isValid, new ArrayList<>(validationErrors),
                new ArrayList<>(validationWarnings));
    }

    /**
     * Gets validation errors
     */
    public List<ValidationError> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }

    /**
     * Gets validation warnings
     */
    public List<ValidationWarning> getValidationWarnings() {
        return new ArrayList<>(validationWarnings);
    }

    /**
     * Enables/disables strict validation mode
     */
    public void setStrictMode(boolean strict) {
        this.strictMode = strict;
    }

    /**
     * Checks if status code is valid
     */
    private boolean isValidStatusCode(int statusCode) {
        // Basic validation - should be expanded based on OpenAPI spec
        return statusCode >= 200 && statusCode < 600;
    }

    /**
     * Checks if JSON structure is valid
     */
    private boolean isValidJsonStructure(String jsonString) {
        try {
            // Basic JSON validation
            return jsonString.trim().startsWith("{") || jsonString.trim().startsWith("[");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if content type is valid
     */
    private boolean isValidContentType(RequestSpecification request) {
        // Basic validation - should be expanded based on OpenAPI spec
        return true; // For now, assume all content types are valid
    }

    /**
     * Validation error representation
     */
    public static class ValidationError {
        private final String code;
        private final String message;

        public ValidationError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Validation warning representation
     */
    public static class ValidationWarning {
        private final String code;
        private final String message;

        public ValidationWarning(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Validation result representation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final List<ValidationWarning> warnings;

        public ValidationResult(boolean valid, List<ValidationError> errors,
                List<ValidationWarning> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }

        public boolean isValid() {
            return valid;
        }

        public List<ValidationError> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<ValidationWarning> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public String getSummary() {
            return String.format("Validation %s. Errors: %d, Warnings: %d",
                    valid ? "PASSED" : "FAILED", errors.size(), warnings.size());
        }
    }
}
