package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB document for JourneyInstance entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "journey_instances")
public class JourneyInstanceDocument extends BaseDocument {

    private String journeyDefinitionId;

    private Integer journeyVersion;

    private String currentState;

    private String status;

    private Map<String, Object> context;

    private Map<String, Object> metadata;

    private String startedAt;

    private String completedAt;

    private String lastActivityAt;
}
