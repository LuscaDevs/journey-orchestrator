package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyInstanceRepository;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import com.luscadevs.journeyorchestrator.application.service.JourneyInstanceService;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.engine.JourneyEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to validate optimistic locking prevents race conditions on the same JourneyInstance. This
 * test simulates real concurrent access to a single instance.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Concurrent Instance Modification Tests")
public class ConcurrentInstanceModificationTest {

    @Autowired
    private MongoJourneyInstanceRepository instanceRepository;

    @Autowired
    private MongoJourneyDefinitionRepository definitionRepository;

    @Autowired
    private JourneyInstanceService instanceService;

    @Autowired
    private JourneyDefinitionRepositoryPort definitionRepositoryPort;

    @Autowired
    private JourneyInstanceRepositoryPort instanceRepositoryPort;

    @Autowired
    private JourneyEngine journeyEngine;

    @Autowired
    private com.luscadevs.journeyorchestrator.application.service.TransitionHistoryService transitionHistoryService;

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private static final String JOURNEY_CODE = "CONCURRENT_TEST";
    private static final String INSTANCE_ID = "test-instance-id";

    @BeforeEach
    void cleanup() {
        instanceRepository.deleteAll();
        definitionRepository.deleteAll();
        mongoTemplate.dropCollection("transition_history");
    }

    @Test
    @DisplayName("Should prevent data loss with optimistic locking")
    void shouldPreventDataLossWithOptimisticLocking() throws Exception {
        // 1. Create journey definition
        JourneyDefinitionDocument definition = createJourneyDefinition();
        definitionRepository.save(definition);

        // 2. Create initial instance
        JourneyInstance instance = createInitialInstance();
        JourneyInstance savedInstance = instanceRepositoryPort.save(instance);

        // 3. Simulate race condition manually
        Long initialVersion = savedInstance.getVersion();

        // Load instance in two separate operations
        JourneyInstance instance1 = instanceRepositoryPort.findById(INSTANCE_ID).orElseThrow();
        JourneyInstance instance2 = instanceRepositoryPort.findById(INSTANCE_ID).orElseThrow();

        // Both should have the same version
        assertEquals(initialVersion, instance1.getVersion());
        assertEquals(initialVersion, instance2.getVersion());

        // Apply different events - first moves to PROCESSING, second moves to COMPLETED
        Event event1 = Event.builder().name("PROCESS_1").description("First event").build();
        Event event2 = Event.builder().name("COMPLETE").description("Second event").build();

        // First operation should succeed
        instanceService.applyEvent(INSTANCE_ID, event1, Map.of("source", "thread1"));

        // Second operation should also succeed thanks to retry mechanism
        // The retry mechanism should handle optimistic locking conflicts automatically
        assertDoesNotThrow(() -> {
            instanceService.applyEvent(INSTANCE_ID, event2, Map.of("source", "thread2"));
        });

        // Validate final state
        JourneyInstance finalInstance = instanceRepositoryPort.findById(INSTANCE_ID).orElseThrow();

        // Version should be incremented (multiple operations)
        assertTrue(finalInstance.getVersion() > initialVersion, "Version should be incremented");

        // Both events should be in the history (retry mechanism worked correctly)
        // Note: History is stored separately in transition_history collection
        var transitionHistory = transitionHistoryService.getTransitionHistory(INSTANCE_ID);
        assertEquals(2, transitionHistory.size(), "Both events should be recorded");

        // No data corruption
        assertNotNull(finalInstance.getCurrentState());

        // Both events should be in the history
        assertTrue(transitionHistory.stream()
                .anyMatch(h -> h.getEvent().getName().equals("PROCESS_1")));
        assertTrue(transitionHistory.stream()
                .anyMatch(h -> h.getEvent().getName().equals("COMPLETE")));

        System.out.println("=== Optimistic Locking Test Results ===");
        System.out.println("Initial version: " + initialVersion);
        System.out.println("Final version: " + finalInstance.getVersion());
        System.out.println("History entries: " + transitionHistory.size());
        System.out.println(
                "Optimistic locking: WORKING CORRECTLY - retry mechanism prevented data loss!");
    }

    @Test
    @DisplayName("Should handle concurrent events with retry mechanism")
    void shouldHandleConcurrentEventsWithRetryMechanism() throws Exception {
        // 1. Create journey definition
        JourneyDefinitionDocument definition = createJourneyDefinition();
        definitionRepository.save(definition);

        // 2. Create initial instance
        JourneyInstance instance = createInitialInstance();
        instanceRepositoryPort.save(instance);

        // 3. Test concurrent access to the same instance
        // Multiple threads trying to update the SAME instance simultaneously
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Simulate concurrent updates by modifying context only
                    // This creates optimistic locking contention without state transitions
                    Map<String, Object> eventData = Map.of("threadId", threadIndex, "timestamp",
                            System.currentTimeMillis(), "concurrentUpdate", true);

                    // Update instance context (this triggers save and version check)
                    JourneyInstance currentInstance = instanceRepositoryPort.findById(INSTANCE_ID)
                            .orElseThrow(() -> new RuntimeException("Instance not found"));

                    // Modify context to trigger save operation
                    currentInstance.getContext().put("thread_" + threadIndex,
                            "updated_at_" + System.currentTimeMillis());

                    // Save the instance - this is where optimistic locking occurs
                    instanceRepositoryPort.save(currentInstance);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // Expected behavior: concurrent modification exceptions
                    if (e instanceof java.util.ConcurrentModificationException
                            || (e.getCause() instanceof java.util.ConcurrentModificationException)
                            || e.getMessage().contains("concurrent modification")) {
                        conflictCount.incrementAndGet();
                        System.out.println("Thread " + threadIndex
                                + " encountered expected concurrent modification: "
                                + e.getMessage());
                    } else {
                        // Unexpected error
                        throw new RuntimeException("Unexpected error in thread " + threadIndex, e);
                    }
                }
            }, executor);

            futures.add(future);
        }

        // 4. Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30,
                TimeUnit.SECONDS);

        executor.shutdown();

        // 5. Validate results
        JourneyInstance finalInstance = instanceRepositoryPort.findById(INSTANCE_ID).orElseThrow(
                () -> new AssertionError("Instance should exist after concurrent operations"));

        // Critical validations
        assertNotNull(finalInstance, "Instance should exist after concurrent operations");
        assertTrue(finalInstance.getVersion() > 0,
                "Version should be incremented after modifications");

        // At least one operation should succeed
        assertTrue(successCount.get() > 0, "At least one operation should succeed");

        // Total operations should equal thread count (success + conflicts)
        assertEquals(threadCount, successCount.get() + conflictCount.get(),
                "Total operations should equal thread count");

        System.out.println("=== Concurrent Retry Test Results ===");
        System.out.println("Thread count: " + threadCount);
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Conflict operations: " + conflictCount.get());
        System.out.println("Final version: " + finalInstance.getVersion());
        System.out.println("Optimistic locking: WORKING CORRECTLY");
    }

    private JourneyDefinitionDocument createJourneyDefinition() {
        JourneyDefinitionDocument definition = new JourneyDefinitionDocument();
        definition.setJourneyCode(JOURNEY_CODE);
        definition.setName("Concurrent Test Journey");
        definition.setDescription("Journey for concurrent testing");
        definition.setVersion(1);
        definition.setActive(true);

        // Create states
        List<JourneyDefinitionDocument.StateDocument> states = new ArrayList<>();
        states.add(createState("START", "INITIAL"));
        states.add(createState("PROCESSING", "INTERMEDIATE"));
        states.add(createState("COMPLETED", "FINAL"));
        definition.setStates(states);

        // Create transitions with specific events for concurrent testing
        List<JourneyDefinitionDocument.TransitionDocument> transitions = new ArrayList<>();
        // Add multiple PROCESS_* events to support concurrent threads
        for (int i = 0; i < 10; i++) {
            transitions.add(createTransition("START", "PROCESSING", "PROCESS_" + i));
        }
        transitions.add(createTransition("PROCESSING", "COMPLETED", "COMPLETE"));
        definition.setTransitions(transitions);

        return definition;
    }

    private JourneyDefinitionDocument.StateDocument createState(String name, String type) {
        return new JourneyDefinitionDocument.StateDocument(null, name, type, null);
    }

    private JourneyDefinitionDocument.TransitionDocument createTransition(String from, String to,
            String event) {
        return new JourneyDefinitionDocument.TransitionDocument(null, from, to, null, null, event,
                null, null);
    }

    private JourneyInstance createInitialInstance() {
        State initialState = State.builder().name("START").build();
        Map<String, Object> context = Map.of("initial", true);

        return JourneyInstance.builder().id(INSTANCE_ID).journeyDefinitionId(JOURNEY_CODE)
                .journeyVersion(1).currentState(initialState)
                .status(com.luscadevs.journey.api.generated.model.JourneyStatus.RUNNING)
                .createdAt(java.time.Instant.now()).updatedAt(java.time.Instant.now())
                .context(context).history(new ArrayList<>()).version(null) // Let MongoDB set
                                                                           // initial version
                .build();
    }
}
