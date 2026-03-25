package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.adapter;

import java.util.List;
import java.util.Optional;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyDefinitionDocument;

public class JourneyDefinitionRepositoryAdapter implements JourneyDefinitionRepositoryPort {

    @Override
    public Optional<JourneyDefinition> findByJourneyCodeAndVersion(String id, Integer version) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByJourneyCodeAndVersion'");
    }

    @Override
    public JourneyDefinition save(JourneyDefinition journeyDefinition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    private JourneyDefinitionDocument toDocument(JourneyDefinition journeyDefinition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDocument'");
    }

    private JourneyDefinition fromDocument(JourneyDefinitionDocument document) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromDocument'");
    }

    @Override
    public List<JourneyDefinition> findByCode(String code) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByCode'");
    }

    @Override
    public List<JourneyDefinition> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

}
