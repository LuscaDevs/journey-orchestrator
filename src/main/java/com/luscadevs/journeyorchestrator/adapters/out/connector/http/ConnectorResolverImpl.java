package com.luscadevs.journeyorchestrator.adapters.out.connector.http;

import com.luscadevs.journeyorchestrator.application.port.out.ConnectorResolverPort;
import com.luscadevs.journeyorchestrator.domain.connector.Connector;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Implementation of ConnectorResolverPort using Strategy pattern.
 * 
 * Resolves connector implementations based on connector type.
 * Currently supports HTTP connector, with extensibility for future connector types.
 */
@Component
public class ConnectorResolverImpl implements ConnectorResolverPort {
    
    private final HttpConnector httpConnector;
    private final Map<ConnectorType, Supplier<Connector<? extends ConnectorConfiguration>>> connectorFactories;
    
    public ConnectorResolverImpl(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
        this.connectorFactories = Map.of(
                ConnectorType.HTTP, () -> httpConnector
        );
    }
    
    @Override
    public Connector<? extends ConnectorConfiguration> resolve(ConnectorConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Connector configuration cannot be null");
        }
        return resolve(configuration.getConnectorType());
    }
    
    @Override
    public Connector<? extends ConnectorConfiguration> resolve(ConnectorType connectorType) {
        if (connectorType == null) {
            throw new IllegalArgumentException("Connector type cannot be null");
        }
        
        Supplier<Connector<? extends ConnectorConfiguration>> factory = 
                connectorFactories.get(connectorType);
        
        if (factory == null) {
            throw new IllegalArgumentException(
                    "No connector implementation found for type: " + connectorType);
        }
        
        return factory.get();
    }
    
    @Override
    public boolean isAvailable(ConnectorType connectorType) {
        return connectorFactories.containsKey(connectorType);
    }
}
