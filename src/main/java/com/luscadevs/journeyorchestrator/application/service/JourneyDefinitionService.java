package com.luscadevs.journeyorchestrator.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.dto.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.repository.JourneyDefinitionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JourneyDefinitionService {

    private final JourneyDefinitionRepository repository;

    public JourneyDefinitionDocument createJourney(CreateJourneyDefinitionRequest request) {

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
                            var state = new com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.StateDocument();
                            state.setName(s.getName());
                            state.setType(s.getType());
                            return state;
                        })
                        .toList());

        document.setTransitions(
                request.getTransitions()
                        .stream()
                        .map(t -> {
                            var transition = new com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.TransitionDocument();
                            transition.setSource(t.getSource());
                            transition.setEvent(t.getEvent());
                            transition.setTarget(t.getTarget());
                            return transition;
                        })
                        .toList());

        return repository.save(document);
    }

}