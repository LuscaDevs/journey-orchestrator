package com.luscadevs.journeyorchestrator.api.dto.connector;

import com.luscadevs.journeyorchestrator.domain.connector.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for HTTP connector configuration.
 * 
 * Extends base connector configuration with HTTP-specific parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HttpConnectorConfigurationDto extends ConnectorConfigurationDto {
    
    private HttpMethod method;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String body;
    
    public static HttpConnectorConfigurationDtoBuilder builder() {
        return new HttpConnectorConfigurationDtoBuilder();
    }
    
    public static class HttpConnectorConfigurationDtoBuilder {
        private String connectorType = "HTTP";
        private String timeout = "PT30S";
        private Boolean retryable = false;
        private HttpMethod method;
        private String url;
        private Map<String, String> headers;
        private Map<String, String> queryParams;
        private String body;
        
        public HttpConnectorConfigurationDtoBuilder connectorType(String connectorType) {
            this.connectorType = connectorType;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder timeout(String timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder retryable(Boolean retryable) {
            this.retryable = retryable;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder method(HttpMethod method) {
            this.method = method;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder url(String url) {
            this.url = url;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }
        
        public HttpConnectorConfigurationDtoBuilder body(String body) {
            this.body = body;
            return this;
        }
        
        public HttpConnectorConfigurationDto build() {
            HttpConnectorConfigurationDto dto = new HttpConnectorConfigurationDto();
            dto.setConnectorType(connectorType);
            dto.setTimeout(timeout);
            dto.setRetryable(retryable);
            dto.setMethod(method);
            dto.setUrl(url);
            dto.setHeaders(headers);
            dto.setQueryParams(queryParams);
            dto.setBody(body);
            return dto;
        }
    }
}
