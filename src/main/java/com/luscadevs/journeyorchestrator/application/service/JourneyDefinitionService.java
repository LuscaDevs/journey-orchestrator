package com.luscadevs.journeyorchestrator.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyDefinitionMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JourneyDefinitionService {

        private final JourneyDefinitionRepositoryPort repository;

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
         * Get the next version number for a journey code. If no versions exist, returns 1.
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

}
