package com.luscadevs.journeyorchestrator.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyDefinitionMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionAlreadyExistsException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.validation.JourneyDefinitionValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JourneyDefinitionService {

        private final JourneyDefinitionRepositoryPort repository;
        private final JourneyDefinitionValidator validator;

        public JourneyDefinition createJourneyDefinition(CreateJourneyDefinitionRequest request) {

                JourneyDefinition definition = JourneyDefinitionMapper.toDomain(request);

                // If version is not specified, automatically assign next version
                JourneyDefinition finalDefinition;
                if (definition.getVersion() == null) {
                        Integer nextVersion = getNextVersion(definition.getJourneyCode());
                        finalDefinition = definition.toBuilder().version(nextVersion).build();
                } else {
                        finalDefinition = definition;
                }

                // Validate DSL structure before saving
                validator.validate(finalDefinition);

                // Check if journey definition already exists
                repository.findByJourneyCodeAndVersion(finalDefinition.getJourneyCode(),
                                finalDefinition.getVersion()).ifPresent(existing -> {
                                        throw new JourneyDefinitionAlreadyExistsException(
                                                        finalDefinition.getJourneyCode() + ":"
                                                                        + finalDefinition
                                                                                        .getVersion());
                                });

                repository.save(finalDefinition);

                return finalDefinition;
        }

        /**
         * Get the next version number for a journey code. If no versions exist, returns
         * 1.
         * Otherwise, returns latest version + 1.
         */
        private Integer getNextVersion(String journeyCode) {
                return repository.findLatestVersion(journeyCode)
                                .map(latest -> latest.getVersion() + 1).orElse(1);
        }

        public List<JourneyDefinition> getJourneyDefinitionsByCode(String code) {
                return repository.findByCode(code)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(code));
        }

        public JourneyDefinition getJourneyDefinition(String id, Integer version) {
                return repository.findByJourneyCodeAndVersion(id, version).orElseThrow(
                                () -> new JourneyDefinitionNotFoundException(id + ":" + version));
        }

        public List<JourneyDefinition> getAllJourneyDefinitions() {
                return repository.findAll();
        }

        public void deleteJourneyDefinition(String id) {
                JourneyDefinition definition = repository.findById(id)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(id));
                repository.delete(definition);
        }

        public JourneyDefinition updateJourneyDefinition(String id,
                        CreateJourneyDefinitionRequest request) {
                // Retrieve existing definition
                JourneyDefinition existing = repository.findById(id)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(id));

                // Map request to definition (without id/createdAt)
                JourneyDefinition incoming = JourneyDefinitionMapper.toDomain(request);

                // Merge states: preserve connectorConfiguration for SERVICE_TASK when omitted.
                // Look up by name first, then by UUID as fallback.
                java.util.Map<String, com.luscadevs.journeyorchestrator.domain.journey.State> existingStateByName = existing
                                .getStates().stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                com.luscadevs.journeyorchestrator.domain.journey.State::getName,
                                                s -> s));

                java.util.Map<java.util.UUID, com.luscadevs.journeyorchestrator.domain.journey.State> existingStateById = existing
                                .getStates().stream()
                                .filter(s -> s.getId() != null)
                                .collect(java.util.stream.Collectors.toMap(
                                                com.luscadevs.journeyorchestrator.domain.journey.State::getId,
                                                s -> s));

                java.util.List<com.luscadevs.journeyorchestrator.domain.journey.State> mergedStates = incoming
                                .getStates().stream()
                                .map(s -> {
                                        if (s.getType() == com.luscadevs.journeyorchestrator.domain.journey.StateType.SERVICE_TASK
                                                        && s.getConnectorConfiguration() == null) {
                                                // Try name-based lookup first, then ID-based
                                                com.luscadevs.journeyorchestrator.domain.journey.State prev = existingStateByName
                                                                .get(s.getName());
                                                if (prev == null && s.getId() != null) {
                                                        prev = existingStateById.get(s.getId());
                                                }
                                                if (prev != null && prev.getConnectorConfiguration() != null) {
                                                        return com.luscadevs.journeyorchestrator.domain.journey.State
                                                                        .builder()
                                                                        .id(s.getId())
                                                                        .name(s.getName())
                                                                        .type(s.getType())
                                                                        .position(s.getPosition())
                                                                        .connectorConfiguration(prev
                                                                                        .getConnectorConfiguration())
                                                                        .build();
                                                }
                                        }
                                        return s;
                                })
                                .collect(java.util.stream.Collectors.toList());

                // Resolve the initialState from the merged states list (the INITIAL-typed
                // state)
                com.luscadevs.journeyorchestrator.domain.journey.State mergedInitialState = mergedStates
                                .stream()
                                .filter(s -> s.getType() == com.luscadevs.journeyorchestrator.domain.journey.StateType.INITIAL)
                                .findFirst()
                                .orElse(incoming.getInitialState());

                // Build merged definition
                JourneyDefinition mergedDefinition = existing.toBuilder()
                                .journeyCode(incoming.getJourneyCode())
                                .name(incoming.getName())
                                .states(mergedStates)
                                .initialState(mergedInitialState)
                                .transitions(incoming.getTransitions())
                                .version(incoming.getVersion())
                                .updatedAt(java.time.Instant.now())
                                .build();

                // Validate merged definition
                validator.validate(mergedDefinition);

                // Persist and return
                repository.save(mergedDefinition);
                return mergedDefinition;
        }

        public JourneyDefinition updateJourneyDefinitionStatus(String id, String status) {

                JourneyDefinition existing = repository.findById(id)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(id));

                // Update the status
                JourneyDefinition updatedDefinition = existing.toBuilder().status(
                                com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinitionStatus
                                                .valueOf(status))
                                .build();

                repository.save(updatedDefinition);

                return updatedDefinition;
        }
}
