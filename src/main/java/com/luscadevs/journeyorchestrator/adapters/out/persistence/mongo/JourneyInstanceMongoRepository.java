package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public interface JourneyInstanceMongoRepository
        extends MongoRepository<JourneyInstance, String> {
}