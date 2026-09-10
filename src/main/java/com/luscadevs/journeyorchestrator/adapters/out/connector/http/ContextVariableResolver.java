package com.luscadevs.journeyorchestrator.adapters.out.connector.http;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for resolving context variable placeholders in connector configurations.
 * 
 * Supports ${variableName} syntax for variable substitution.
 * Variables are resolved from the journey instance context.
 * Supports dot notation for nested access (e.g., ${customer.id}).
 */
@Component
public class ContextVariableResolver {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * Resolves placeholders in a string using the journey instance context.
     * 
     * @param input The input string possibly containing placeholders
     * @param instance The journey instance containing the context
     * @return The string with placeholders resolved to their values
     */
    public String resolve(String input, JourneyInstance instance) {
        if (input == null || input.isBlank()) {
            return input;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = instance.getVariable(variableName);
            
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Resolves placeholders in a map of string values.
     * 
     * @param inputMap The input map possibly containing placeholders in values
     * @param instance The journey instance containing the context
     * @return A new map with placeholders resolved
     */
    public Map<String, String> resolveMap(Map<String, String> inputMap, JourneyInstance instance) {
        if (inputMap == null || inputMap.isEmpty()) {
            return inputMap;
        }
        
        return inputMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> resolve(entry.getValue(), instance)
                ));
    }
}
