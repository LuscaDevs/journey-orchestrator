package com.luscadevs.journeyorchestrator.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.dto.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.StateDocument;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.TransitionDocument;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.repository.JourneyDefinitionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JourneyDefinitionService {

    private final JourneyDefinitionRepository repository;

    public JourneyDefinitionDocument createJourneyDefinition(CreateJourneyDefinitionRequest request) {

        JourneyDefinitionDocument document = new JourneyDefinitionDocument();

        document.setJourneyCode(request.getJourneyCode());
        document.setVersion(request.getVersion());
        document.setInitialState(request.getInitialState());
        document.setActive(false);
        document.setCreatedAt(Instant.now());

        document.setStates(
                request.getStates()
                        .stream()
                        .map(s -> {
                            var state = new StateDocument();
                            state.setName(s.getName());
                            state.setType(s.getType());
                            return state;
                        })
                        .toList());

        document.setTransitions(
                request.getTransitions()
                        .stream()
                        .map(t -> {
                            var transition = new TransitionDocument();
                            transition.setSource(t.getSource());
                            transition.setEvent(t.getEvent());
                            transition.setTarget(t.getTarget());
                            return transition;
                        })
                        .toList());

        return repository.save(document);
    }

    public JourneyDefinitionDocument getJourneyDefinition(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Journey definition not found"));
    }

    public List<JourneyDefinitionDocument> listJourneyDefinitions() {
        return repository.findAll();
    }

    public void deactivateJourneyDefinition(String id) {
        var document = repository.findById(id).orElseThrow(() -> new RuntimeException("Journey definition not found"));
        document.setActive(false);
        repository.save(document);
    }

}