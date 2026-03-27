package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.EventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for EventDocument.
 */
@Repository
public interface MongoEventRepository extends MongoRepository<EventDocument, String> {

    /**
     * Find events by journey instance ID.
     */
    List<EventDocument> findByJourneyInstanceId(String journeyInstanceId);

    /**
     * Find events by event type.
     */
    List<EventDocument> findByEventType(String eventType);

    /**
     * Find events by journey instance ID and event type.
     */
    List<EventDocument> findByJourneyInstanceIdAndEventType(String journeyInstanceId, String eventType);

    /**
     * Find events by user ID.
     */
    List<EventDocument> findByUserId(String userId);

    /**
     * Find events within a timestamp range.
     */
    @Query("{ 'timestamp' : { $gte: ?0, $lte: ?1 } }")
    List<EventDocument> findEventsBetweenTimestamps(String startTime, String endTime);

    /**
     * Find events for a journey instance within a timestamp range.
     */
    @Query("{ 'journeyInstanceId' : ?0, 'timestamp' : { $gte: ?1, $lte: ?2 } }")
    List<EventDocument> findEventsForJourneyInstanceBetweenTimestamps(String journeyInstanceId, String startTime,
            String endTime);

    /**
     * Find events by state transition.
     */
    List<EventDocument> findByPreviousStateAndNewState(String previousState, String newState);

    /**
     * Find recent events for a journey instance (last N events).
     */
    @Query(value = "{ 'journeyInstanceId' : ?0 }", sort = "{ 'timestamp' : -1 }")
    List<EventDocument> findRecentEventsForJourneyInstance(String journeyInstanceId);

    /**
     * Find events after a specific timestamp.
     */
    @Query("{ 'timestamp' : { $gte: ?0 } }")
    List<EventDocument> findEventsAfterTimestamp(String timestamp);

    /**
     * Count events for a journey instance.
     */
    long countByJourneyInstanceId(String journeyInstanceId);
}
