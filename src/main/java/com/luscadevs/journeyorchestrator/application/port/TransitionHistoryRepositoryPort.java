package com.luscadevs.journeyorchestrator.application.port;

import java.time.Instant;
import java.util.List;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

public interface TransitionHistoryRepositoryPort {
    void save(TransitionHistory event);
    List<TransitionHistory> findByInstanceIdOrderByTimestampAsc(String instanceId);
    List<TransitionHistory> findByInstanceIdAndTimestampBetween(String instanceId, Instant start, Instant end);
    List<TransitionHistory> findByInstanceIdAndEventType(String instanceId, String eventType);
    boolean existsByInstanceId(String instanceId);
    void deleteByInstanceId(String instanceId); // Soft delete
    TransitionHistory findById(TransitionHistoryEventId id);
}
