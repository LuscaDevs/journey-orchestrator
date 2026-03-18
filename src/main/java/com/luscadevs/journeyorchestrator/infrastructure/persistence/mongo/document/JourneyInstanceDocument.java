package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "journey_instances")
public class JourneyInstanceDocument {

    @Id
    private String instanceId;

    private String journeyCode;

    private String version;

    private String currentState;

    private String status;

    private Map<String, Object> context;

    private Instant createdAt;

    private Instant updatedAt;

}