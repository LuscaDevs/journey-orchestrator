package com.luscadevs.journeyorchestrator.domain.exception;

/**
 * Standard error codes for journey definition validation errors.
 * These codes provide semantic, machine-readable identifiers for validation failures
 * that can be used by the frontend for robust error handling without string parsing.
 */
public final class JourneyErrorCodes {

    // State-related errors
    public static final String NO_INITIAL_STATE = "NO_INITIAL_STATE";
    public static final String NO_FINAL_STATE = "NO_FINAL_STATE";
    public static final String STATE_NOT_FOUND = "STATE_NOT_FOUND";
    public static final String DUPLICATE_STATE_NAME = "DUPLICATE_STATE_NAME";
    public static final String DUPLICATE_STATE_ID = "DUPLICATE_STATE_ID";
    public static final String INVALID_STATE_TYPE = "INVALID_STATE_TYPE";
    public static final String STATE_NAME_REQUIRED = "STATE_NAME_REQUIRED";

    // Transition-related errors
    public static final String SOURCE_STATE_NOT_FOUND = "SOURCE_STATE_NOT_FOUND";
    public static final String TARGET_STATE_NOT_FOUND = "TARGET_STATE_NOT_FOUND";
    public static final String INVALID_TRANSITION = "INVALID_TRANSITION";
    public static final String TRANSITION_SOURCE_REQUIRED = "TRANSITION_SOURCE_REQUIRED";
    public static final String TRANSITION_TARGET_REQUIRED = "TRANSITION_TARGET_REQUIRED";
    public static final String TRANSITION_EVENT_REQUIRED = "TRANSITION_EVENT_REQUIRED";
    public static final String TRANSITION_CONFLICT = "TRANSITION_CONFLICT";

    // Condition-related errors
    public static final String INVALID_CONDITION_SYNTAX = "INVALID_CONDITION_SYNTAX";

    // General validation errors
    public static final String INVALID_JOURNEY_CODE = "INVALID_JOURNEY_CODE";
    public static final String INVALID_VERSION = "INVALID_VERSION";
    public static final String INVALID_NAME = "INVALID_NAME";

    private JourneyErrorCodes() {
        // Utility class - prevent instantiation
    }
}
