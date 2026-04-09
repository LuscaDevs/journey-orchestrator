package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;

public interface JourneyInstanceMongoRepository
                extends MongoRepository<JourneyInstanceDocument, String> {
}
