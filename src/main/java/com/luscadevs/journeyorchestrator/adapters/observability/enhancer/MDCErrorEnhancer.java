package com.luscadevs.journeyorchestrator.adapters.observability.enhancer;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Enhances MDC context with error information during exception handling.
 */
@Component
public class MDCErrorEnhancer {
    
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    
    /**
     * Enhances MDC with error information from the exception.
     * 
     * @param exception The exception that occurred
     */
    public void enhanceMDCWithError(Exception exception) {
        if (exception != null) {
            MDC.put(ERROR_CODE, exception.getClass().getSimpleName());
            if (exception.getMessage() != null) {
                MDC.put(ERROR_MESSAGE, exception.getMessage());
            }
        }
    }
    
    /**
     * Clears error-specific MDC fields.
     */
    public void clearErrorMDC() {
        MDC.remove(ERROR_CODE);
        MDC.remove(ERROR_MESSAGE);
    }
}
