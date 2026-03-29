package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "transition_history")
@CompoundIndex(name = "instance_timestamp_idx", def = "{'instanceId': 1, 'timestamp': 1}")
@CompoundIndex(name = "timestamp_idx", def = "{'timestamp': 1}")
public class TransitionHistoryDocument {
    @Id
    private String id;
    private String instanceId;
    private String fromState;
    private String toState;
    private String eventType;
    private String eventData;
    @Indexed
    private Instant timestamp;
    private Map<String, Object> metadata;
    @Indexed
    private Instant createdAt;
    private Instant deletedAt; // For soft deletion
    @Indexed
    private Instant expiresAt; // For TTL cleanup
}
