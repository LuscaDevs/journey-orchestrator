package com.luscadevs.journeyorchestrator.api.dto.connector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Base DTO for connector configuration.
 * 
 * Uses Jackson polymorphic type handling to support different connector types.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "connectorType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpConnectorConfigurationDto.class, name = "HTTP")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorConfigurationDto {
    
    private String connectorType;
    private String timeout;
    private Boolean retryable;
    
    /**
     * Converts ISO 8601 duration string to Duration.
     */
    public Duration getTimeoutAsDuration() {
        if (timeout == null || timeout.isBlank()) {
            return Duration.ofSeconds(30); // Default timeout
        }
        return Duration.parse(timeout);
    }
}
