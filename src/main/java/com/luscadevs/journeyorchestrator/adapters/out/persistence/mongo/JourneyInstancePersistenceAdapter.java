package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import java.util.List;
import java.util.Optional;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public class JourneyInstancePersistenceAdapter implements JourneyInstanceRepositoryPort {
    private final JourneyInstanceMongoRepository mongoRepository;

    public JourneyInstancePersistenceAdapter(JourneyInstanceMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public JourneyInstance save(JourneyInstance journeyInstance) {
        return mongoRepository.save(journeyInstance);
    }

    @Override
    public Optional<JourneyInstance> findById(String instanceId) {
        return mongoRepository.findById(instanceId);
    }

    @Override
    public List<JourneyInstance> findAll() {
        return mongoRepository.findAll();
    }

    @Override
    public void deleteById(String instanceId) {
        mongoRepository.deleteById(instanceId);
    }

}
