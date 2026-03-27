package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB document for Event entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "journey_events")
public class EventDocument extends BaseDocument {
    
    private String journeyInstanceId;
    
    private String eventType;
    
    private String previousState;
    
    private String newState;
    
    private Map<String, Object> eventData;
    
    private Map<String, Object> context;
    
    private String timestamp;
    
    private String userId;
    
    private Map<String, Object> metadata;
}
