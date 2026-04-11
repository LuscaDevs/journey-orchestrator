package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.BaseDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.EventDocumentMapper;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyDefinitionDocumentMapper;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyInstanceDocumentMapper;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.JourneyDefinitionRepositoryImpl;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.JourneyInstanceRepositoryImpl;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyInstanceRepository;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuration class for MongoDB persistence adapters. Enables MongoDB repositories and configures
 * necessary beans.
 */
@Configuration
@EnableMongoRepositories(
        basePackages = "com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository")
@EnableConfigurationProperties(MongoPersistenceProperties.class)
@Profile("!test")
public class MongoAdapterConfiguration {

    @Bean
    public BaseDocument.AuditEventListener auditEventListener() {
        return new BaseDocument.AuditEventListener();
    }

    @Bean
    public JourneyDefinitionRepositoryPort journeyDefinitionRepositoryPort(
            MongoJourneyDefinitionRepository mongoJourneyDefinitionRepository,
            JourneyDefinitionDocumentMapper journeyDefinitionDocumentMapper) {
        return new JourneyDefinitionRepositoryImpl(mongoJourneyDefinitionRepository,
                journeyDefinitionDocumentMapper);
    }

    @Bean
    public JourneyInstanceRepositoryPort journeyInstanceRepositoryPort(
            MongoJourneyInstanceRepository mongoJourneyInstanceRepository,
            JourneyInstanceDocumentMapper journeyInstanceDocumentMapper) {
        return new JourneyInstanceRepositoryImpl(mongoJourneyInstanceRepository,
                journeyInstanceDocumentMapper);
    }

    @Bean
    public JourneyDefinitionDocumentMapper journeyDefinitionDocumentMapper() {
        return new JourneyDefinitionDocumentMapper();
    }

    @Bean
    public JourneyInstanceDocumentMapper journeyInstanceDocumentMapper() {
        return new JourneyInstanceDocumentMapper();
    }

    @Bean
    public EventDocumentMapper eventDocumentMapper() {
        return new EventDocumentMapper();
    }

    /**
     * MongoTemplate is automatically configured by Spring Boot. No need to define it manually here.
     */
}
