package com.luscadevs.journeyorchestrator.domain.connector;

/**
 * Enum representing the type of connector to be used.
 * 
 * This enum identifies the connector type for journey SERVICE_TASK states.
 * Future connector types can be added without modifying runtime core logic.
 */
public enum ConnectorType {
    /**
     * HTTP-based connector for REST API integrations.
     */
    HTTP,
    
    /**
     * Future connector types (reserved for future implementations):
     * - KAFKA: Apache Kafka message broker
     * - RABBITMQ: RabbitMQ message broker
     * - WEBHOOK: Webhook-based integrations
     * - GRPC: gRPC protocol-based integrations
     * - GRAPHQL: GraphQL query-based integrations
     * - SCRIPT: Script execution connectors
     */
    // KAFKA,
    // RABBITMQ,
    // WEBHOOK,
    // GRPC,
    // GRAPHQL,
    // SCRIPT
}
