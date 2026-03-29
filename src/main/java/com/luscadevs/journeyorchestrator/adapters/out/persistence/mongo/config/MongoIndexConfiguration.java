package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import jakarta.annotation.PostConstruct;

/**
 * MongoDB index configuration for optimal query performance.
 */
@Configuration
@Slf4j
public class MongoIndexConfiguration {

        private final MongoTemplate mongoTemplate;

        public MongoIndexConfiguration(MongoTemplate mongoTemplate) {
                this.mongoTemplate = mongoTemplate;
        }

        @PostConstruct
        public void createIndexes() {
                // Skip index creation in test environment to avoid conflicts
                if (isTestEnvironment()) {
                        return;
                }

                try {
                        // Journey Definition indexes
                        createJourneyDefinitionIndexes();

                        // Journey Instance indexes
                        createJourneyInstanceIndexes();

                        // Event indexes
                        createEventIndexes();
                } catch (Exception e) {
                        // Log the error but don't fail startup - indexes may already exist
                        System.err.println("Warning: Could not create MongoDB indexes: " + e.getMessage());
                        // In production, you might want to use proper logging
                }
        }

        /**
         * Checks if we're running in a test environment.
         */
        private boolean isTestEnvironment() {
                // Check if we're running in a test context
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                        String className = element.getClassName();
                        if (className.contains("Test") ||
                                        className.contains("junit") ||
                                        className.contains("surefire") ||
                                        className.contains("maven") ||
                                        className.contains("gradle")) {
                                return true;
                        }
                }
                return false;
        }

        private void createJourneyDefinitionIndexes() {
                // Unique index on journey code and version
                mongoTemplate.indexOps("journey_definitions")
                                .ensureIndex(new Index()
                                                .on("journeyCode", Sort.Direction.ASC)
                                                .on("version", Sort.Direction.ASC)
                                                .named("idx_journey_code_version_unique")
                                                .unique());

                // Index on name for search
                mongoTemplate.indexOps("journey_definitions")
                                .ensureIndex(new Index()
                                                .on("name", Sort.Direction.ASC)
                                                .named("idx_name"));

                // Index on active status
                mongoTemplate.indexOps("journey_definitions")
                                .ensureIndex(new Index()
                                                .on("active", Sort.Direction.ASC)
                                                .named("idx_active"));
        }

        private void createJourneyInstanceIndexes() {
                // Index on journey definition ID
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("journeyDefinitionId", Sort.Direction.ASC)
                                                .named("idx_journey_definition_id"));

                // Index on current state
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("currentState", Sort.Direction.ASC)
                                                .named("idx_current_state"));

                // Index on status
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("status", Sort.Direction.ASC)
                                                .named("idx_status"));

                // Compound index on journey definition ID and status
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("journeyDefinitionId", Sort.Direction.ASC)
                                                .on("status", Sort.Direction.ASC)
                                                .named("idx_journey_definition_id_status"));

                // Index on started at for time-based queries
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("startedAt", Sort.Direction.ASC)
                                                .named("idx_started_at"));

                // Index on last activity at for activity tracking
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("lastActivityAt", Sort.Direction.ASC)
                                                .named("idx_last_activity_at"));

                // TTL index for completed instances (optional - 30 days)
                mongoTemplate.indexOps("journey_instances")
                                .ensureIndex(new Index()
                                                .on("completedAt", Sort.Direction.ASC)
                                                .named("idx_completed_at_ttl")
                                                .expire(30 * 24 * 60 * 60)); // 30 days in seconds
        }

        private void createEventIndexes() {
                // Index on journey instance ID
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("journeyInstanceId", Sort.Direction.ASC)
                                                .named("idx_journey_instance_id"));

                // Index on event type
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("eventType", Sort.Direction.ASC)
                                                .named("idx_event_type"));

                // Compound index on journey instance ID and event type
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("journeyInstanceId", Sort.Direction.ASC)
                                                .on("eventType", Sort.Direction.ASC)
                                                .named("idx_journey_instance_id_event_type"));

                // Index on timestamp for time-based queries
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("timestamp", Sort.Direction.ASC)
                                                .named("idx_timestamp"));

                // Compound index on journey instance ID and timestamp
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("journeyInstanceId", Sort.Direction.ASC)
                                                .on("timestamp", Sort.Direction.DESC)
                                                .named("idx_journey_instance_id_timestamp"));

                // Index on user ID for user-specific queries
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("userId", Sort.Direction.ASC)
                                                .named("idx_user_id"));

                // Index on state transition
                mongoTemplate.indexOps("journey_events")
                                .ensureIndex(new Index()
                                                .on("previousState", Sort.Direction.ASC)
                                                .on("newState", Sort.Direction.ASC)
                                                .named("idx_state_transition"));

                // TTL index for events (optional - 90 days)
                try {
                        mongoTemplate.indexOps("journey_events")
                                        .ensureIndex(new Index()
                                                        .on("timestamp", Sort.Direction.ASC)
                                                        .named("idx_timestamp_ttl")
                                                        .expire(90 * 24 * 60 * 60)); // 90 days in seconds
                } catch (Exception e) {
                        log.warn("Could not create TTL index for journey_events, it may already exist: {}",
                                        e.getMessage());
                }
        }
}
