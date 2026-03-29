package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import java.time.Instant;
import java.util.List;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.TransitionHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransitionHistoryMongoRepository extends MongoRepository<TransitionHistoryDocument, String> {

        List<TransitionHistoryDocument> findByInstanceIdOrderByTimestampAsc(String instanceId);

        List<TransitionHistoryDocument> findByInstanceIdAndTimestampBetweenOrderByTimestampAsc(
                        String instanceId, Instant start, Instant end);

        List<TransitionHistoryDocument> findByInstanceIdAndEventTypeOrderByTimestampAsc(
                        String instanceId, String eventType);

        boolean existsByInstanceId(String instanceId);

        @Query("{ 'instanceId': ?0, 'deletedAt': null }")
        List<TransitionHistoryDocument> findByInstanceIdAndDeletedAtIsNull(String instanceId);

        @Query("{ 'instanceId': ?0, 'deletedAt': { $ne: null } }")
        List<TransitionHistoryDocument> findByInstanceIdAndDeletedAtIsNotNull(String instanceId);

        @Query("{ 'expiresAt': { $lte: ?0 } }")
        List<TransitionHistoryDocument> findByExpiresAtBefore(Instant now);

        @Query("{ 'instanceId': ?0 }")
        void softDeleteByInstanceId(String instanceId);
}
