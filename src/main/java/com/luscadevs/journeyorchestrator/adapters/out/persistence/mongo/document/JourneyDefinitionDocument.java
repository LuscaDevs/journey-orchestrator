package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
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
@CompoundIndex(name = "states_id_idx", def = "{'states.id': 1}")
@CompoundIndex(name = "transitions_sourceStateId_idx", def = "{'transitions.sourceStateId': 1}")
@CompoundIndex(name = "transitions_targetStateId_idx", def = "{'transitions.targetStateId': 1}")
public class JourneyDefinitionDocument extends BaseDocument {

    private String journeyCode;

    private String name;

    private String description;

    private Integer version;

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
        private String id; // UUID for unique state identification
        private String name;
        private String type;
        private Position position; // Visual editor position data
    }

    /**
     * Position value object for visual editor.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private BigDecimal x;
        private BigDecimal y;
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
        private String sourceStateId; // UUID for ID-based source reference
        private String targetStateId; // UUID for ID-based target reference
        private String event;
        private String condition;
        private Map<String, Object> metadata;
    }
}
