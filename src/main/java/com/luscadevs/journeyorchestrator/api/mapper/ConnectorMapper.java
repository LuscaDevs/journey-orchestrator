package com.luscadevs.journeyorchestrator.api.mapper;

import com.luscadevs.journeyorchestrator.api.dto.connector.ConnectorConfigurationDto;
import com.luscadevs.journeyorchestrator.api.dto.connector.HttpConnectorConfigurationDto;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between connector DTOs and domain entities.
 */
@Component
public class ConnectorMapper {
    
    /**
     * Converts a connector configuration DTO to a domain entity.
     * 
     * @param dto The connector configuration DTO
     * @return The connector configuration domain entity
     */
    public ConnectorConfiguration toDomain(ConnectorConfigurationDto dto) {
        if (dto == null) {
            return null;
        }
        
        if (dto instanceof HttpConnectorConfigurationDto) {
            return toHttpDomain((HttpConnectorConfigurationDto) dto);
        }
        
        throw new IllegalArgumentException("Unsupported connector type: " + dto.getConnectorType());
    }
    
    /**
     * Converts an HTTP connector configuration DTO to a domain entity.
     * 
     * @param dto The HTTP connector configuration DTO
     * @return The HTTP connector configuration domain entity
     */
    public HttpConnectorConfiguration toHttpDomain(HttpConnectorConfigurationDto dto) {
        if (dto == null) {
            return null;
        }
        
        return new HttpConnectorConfiguration(
                dto.getMethod(),
                dto.getUrl(),
                dto.getHeaders(),
                dto.getQueryParams(),
                dto.getBody(),
                dto.getTimeoutAsDuration()
        );
    }
    
    /**
     * Converts a connector configuration domain entity to a DTO.
     * 
     * @param configuration The connector configuration domain entity
     * @return The connector configuration DTO
     */
    public ConnectorConfigurationDto toDto(ConnectorConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        
        if (configuration instanceof HttpConnectorConfiguration) {
            return toHttpDto((HttpConnectorConfiguration) configuration);
        }
        
        throw new IllegalArgumentException("Unsupported connector configuration type: " + 
                configuration.getClass().getSimpleName());
    }
    
    /**
     * Converts an HTTP connector configuration domain entity to a DTO.
     * 
     * @param configuration The HTTP connector configuration domain entity
     * @return The HTTP connector configuration DTO
     */
    public HttpConnectorConfigurationDto toHttpDto(HttpConnectorConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        
        return HttpConnectorConfigurationDto.builder()
                .connectorType(configuration.getConnectorType().name())
                .timeout(configuration.getTimeout().toString())
                .retryable(configuration.isRetryable())
                .method(configuration.getMethod())
                .url(configuration.getUrl())
                .headers(configuration.getHeaders())
                .queryParams(configuration.getQueryParams())
                .body(configuration.getBody())
                .build();
    }
}
