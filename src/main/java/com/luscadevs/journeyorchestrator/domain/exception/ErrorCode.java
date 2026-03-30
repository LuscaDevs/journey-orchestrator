package com.luscadevs.journeyorchestrator.domain.exception;

/**
 * Standardized error codes for the Journey Orchestrator application. Each error code provides a
 * unique identifier and default message for programmatic handling.
 */
public enum ErrorCode {
    // Journey Definition Errors
    JOURNEY_DEFINITION_NOT_FOUND("JOURNEY_001",
            "Journey definition not found"), JOURNEY_DEFINITION_ALREADY_EXISTS("JOURNEY_002",
                    "Journey definition already exists"), JOURNEY_DEFINITION_INVALID("JOURNEY_003",
                            "Journey definition is invalid"),

    // Journey Instance Errors
    JOURNEY_INSTANCE_NOT_FOUND("JOURNEY_101",
            "Journey instance not found"), JOURNEY_ALREADY_COMPLETED("JOURNEY_102",
                    "Journey has already completed"), JOURNEY_ALREADY_STARTED("JOURNEY_103",
                            "Journey has already started"),

    // State Transition Errors
    INVALID_STATE_TRANSITION("STATE_001", "Invalid state transition"), STATE_TRANSITION_NOT_ALLOWED(
            "STATE_002", "State transition not allowed in current state"),

    // Validation Errors
    VALIDATION_FAILED("VALIDATION_001", "Validation failed"), INVALID_REQUEST_FORMAT(
            "VALIDATION_002", "Invalid request format"), MISSING_REQUIRED_FIELD("VALIDATION_003",
                    "Missing required field"), INVALID_CONDITION_SYNTAX("VALIDATION_004",
                            "Invalid condition syntax"),

    // Conflict Errors
    CONCURRENT_MODIFICATION("CONFLICT_001", "Concurrent modification detected"), RESOURCE_LOCKED(
            "CONFLICT_002", "Resource is currently locked"),

    // System Errors
    INTERNAL_SERVER_ERROR("SYSTEM_001", "Internal server error"), DATABASE_ERROR("SYSTEM_002",
            "Database operation failed"), EXTERNAL_SERVICE_ERROR("SYSTEM_003",
                    "External service error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
