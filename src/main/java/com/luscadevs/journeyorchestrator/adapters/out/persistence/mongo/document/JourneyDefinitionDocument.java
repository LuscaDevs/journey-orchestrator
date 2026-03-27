package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * MongoDB document for JourneyDefinition entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "journey_definitions")
public class JourneyDefinitionDocument extends BaseDocument {

    private String journeyCode;

    private String name;

    private String description;

    private String version;

    private boolean active;

    private List<StateDocument> states;

    private List<TransitionDocument> transitions;

    private Map<String, Object> metadata;

    /**
     * Nested class for state documents.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateDocument {
        private String name;
        private String type;
    }

    /**
     * Nested class for transition documents.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransitionDocument {
        private String id;
        private String fromState;
        private String toState;
        private String event;
        private String condition;
        private Map<String, Object> metadata;
    }
}
