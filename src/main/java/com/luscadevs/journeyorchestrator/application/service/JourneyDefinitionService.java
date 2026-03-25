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

                // Check if journey definition already exists
                repository.findByJourneyCodeAndVersion(definition.getJourneyCode(), definition.getVersion())
                                .ifPresent(existing -> {
                                        throw new JourneyDefinitionAlreadyExistsException(
                                                        definition.getJourneyCode() + ":" + definition.getVersion());
                                });

                repository.save(definition);

                return definition;
        }

        public List<JourneyDefinition> getJourneyDefinitionsByCode(String code) {
                return repository.findByCode(code)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(code));
        }

        public JourneyDefinition getJourneyDefinition(String id, Integer version) {
                return repository
                                .findByJourneyCodeAndVersion(id, version)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(id + ":" + version));
        }

        public List<JourneyDefinition> getAllJourneyDefinitions() {
                return repository.findAll();
        }

}