package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyInstanceDocument;

public interface JourneyInstanceRepository extends MongoRepository<JourneyInstanceDocument, String> {

}
