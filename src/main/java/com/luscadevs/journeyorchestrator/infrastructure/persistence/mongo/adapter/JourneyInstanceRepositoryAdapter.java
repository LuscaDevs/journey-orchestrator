package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.adapter;

import java.util.List;
import java.util.Optional;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyInstanceDocument;

public class JourneyInstanceRepositoryAdapter implements JourneyInstanceRepositoryPort {

    @Override
    public JourneyInstance save(JourneyInstance journeyInstance) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Optional<JourneyInstance> findById(String instanceId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public List<JourneyInstance> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public void deleteById(String instanceId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    private JourneyInstanceDocument toDocument(JourneyInstance journeyInstance) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDocument'");
    }

    private JourneyInstance fromDocument(JourneyInstanceDocument document) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromDocument'");
    }

}
