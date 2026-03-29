package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.TransitionHistoryMongoRepository;
import com.luscadevs.journeyorchestrator.application.port.TransitionHistoryRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransitionHistoryRepositoryImpl implements TransitionHistoryRepositoryPort {

    private final TransitionHistoryMongoRepository mongoRepository;
    private final TransitionHistoryDocumentMapper mapper;

    @Override
    public void save(TransitionHistory event) {
        log.debug("Saving transition history event: {}", event.getId().getValue());

        TransitionHistoryDocument document = mapper.toDocument(event);
        mongoRepository.save(document);

        log.debug("Successfully saved transition history event: {}", event.getId().getValue());
    }

    @Override
    public List<TransitionHistory> findByInstanceIdOrderByTimestampAsc(String instanceId) {
        log.debug("Finding transition history for instance: {} ordered by timestamp", instanceId);

        List<TransitionHistoryDocument> documents = mongoRepository
                .findByInstanceIdOrderByTimestampAsc(instanceId);
        List<TransitionHistory> events = documents.stream()
                .map(mapper::toDomain)
                .toList();

        log.debug("Found {} transition events for instance: {}", events.size(), instanceId);
        return events;
    }

    @Override
    public List<TransitionHistory> findByInstanceIdAndTimestampBetween(String instanceId, Instant start, Instant end) {
        log.debug("Finding transition history for instance: {} between {} and {}", instanceId, start, end);

        List<TransitionHistoryDocument> documents = mongoRepository
                .findByInstanceIdAndTimestampBetweenOrderByTimestampAsc(instanceId, start, end);
        List<TransitionHistory> events = documents.stream()
                .map(mapper::toDomain)
                .toList();

        log.debug("Found {} transition events for instance: {} in date range", events.size(), instanceId);
        return events;
    }

    @Override
    public List<TransitionHistory> findByInstanceIdAndEventType(String instanceId, String eventType) {
        log.debug("Finding transition history for instance: {} filtered by event type: {}", instanceId, eventType);

        List<TransitionHistoryDocument> documents = mongoRepository
                .findByInstanceIdAndEventTypeOrderByTimestampAsc(instanceId, eventType);
        List<TransitionHistory> events = documents.stream()
                .map(mapper::toDomain)
                .toList();

        log.debug("Found {} transition events for instance: {} with event type: {}", events.size(), instanceId,
                eventType);
        return events;
    }

    @Override
    public boolean existsByInstanceId(String instanceId) {
        log.debug("Checking if transition history exists for instance: {}", instanceId);

        boolean exists = mongoRepository.existsByInstanceId(instanceId);

        log.debug("Transition history exists for instance {}: {}", instanceId, exists);
        return exists;
    }

    @Override
    public void deleteByInstanceId(String instanceId) {
        log.warn("Soft deleting transition history for instance: {}", instanceId);

        // In a real implementation, we would update the deletedAt field
        // For now, we'll use the hard delete approach
        List<TransitionHistoryDocument> documents = mongoRepository
                .findByInstanceIdOrderByTimestampAsc(instanceId);
        mongoRepository.deleteAll(documents);

        log.debug("Successfully deleted transition history for instance: {}", instanceId);
    }

    @Override
    public TransitionHistory findById(TransitionHistoryEventId id) {
        log.debug("Finding transition history event by ID: {}", id.getValue());

        Optional<TransitionHistoryDocument> document = mongoRepository.findById(id.getValue());
        TransitionHistory event = document.map(mapper::toDomain).orElse(null);

        if (event != null) {
            log.debug("Found transition history event: {}", id.getValue());
        } else {
            log.debug("Transition history event not found: {}", id.getValue());
        }

        return event;
    }
}
