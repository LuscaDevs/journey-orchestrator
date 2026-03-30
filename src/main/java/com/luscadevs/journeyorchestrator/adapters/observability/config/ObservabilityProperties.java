package com.luscadevs.journeyorchestrator.adapters.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration properties for execution observability feature.
 * Controls logging behavior, performance thresholds, and data exclusion rules.
 */
@Component
@ConfigurationProperties("observability.logging")
public class ObservabilityProperties {
    
    private boolean enabled = true;
    private Set<String> excludedMethods = new HashSet<>();
    private Set<String> excludedPackages = new HashSet<>();
    private long slowOperationThreshold = 1000; // ms
    private boolean logParameters = false; // Always false for security
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<String> getExcludedMethods() {
        return excludedMethods;
    }
    
    public void setExcludedMethods(Set<String> excludedMethods) {
        this.excludedMethods = excludedMethods;
    }
    
    public Set<String> getExcludedPackages() {
        return excludedPackages;
    }
    
    public void setExcludedPackages(Set<String> excludedPackages) {
        this.excludedPackages = excludedPackages;
    }
    
    public long getSlowOperationThreshold() {
        return slowOperationThreshold;
    }
    
    public void setSlowOperationThreshold(long slowOperationThreshold) {
        this.slowOperationThreshold = slowOperationThreshold;
    }
    
    public boolean isLogParameters() {
        return logParameters;
    }
    
    public void setLogParameters(boolean logParameters) {
        this.logParameters = logParameters;
    }
}
