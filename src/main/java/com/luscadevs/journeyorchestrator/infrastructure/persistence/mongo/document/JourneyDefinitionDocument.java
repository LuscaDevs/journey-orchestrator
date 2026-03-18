package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "journey_definitions")
public class JourneyDefinitionDocument {

    @Id
    private String id;

    private String journeyCode;

    private String version;

    private String initialState;

    private List<StateDocument> states;

    private List<TransitionDocument> transitions;

    private boolean active;

    private Instant createdAt;

}
