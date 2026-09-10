package com.luscadevs.journeyorchestrator.adapters.out.connector;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging connector execution.
 * 
 * Provides structured logging for all connector operations including
 * execution start, completion, and failure with relevant context.
 */
@Slf4j
@Aspect
@Component
public class ConnectorLoggingAspect {
    
    @Around("execution(* com.luscadevs.journeyorchestrator.application.service.ConnectorExecutionService.executeConnector(..))")
    public Object logConnectorExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String journeyInstanceId = (String) joinPoint.getArgs()[0];
        String stateId = (String) joinPoint.getArgs()[1];
        ConnectorConfiguration configuration = (ConnectorConfiguration) joinPoint.getArgs()[2];
        
        log.info("Connector execution started - JourneyInstance: {}, State: {}, ConnectorType: {}",
                journeyInstanceId, stateId, configuration.getConnectorType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Connector execution completed successfully - JourneyInstance: {}, State: {}, Duration: {}ms",
                    journeyInstanceId, stateId, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Connector execution failed - JourneyInstance: {}, State: {}, Duration: {}ms, Error: {}",
                    journeyInstanceId, stateId, duration, e.getMessage(), e);
            throw e;
        }
    }
}
