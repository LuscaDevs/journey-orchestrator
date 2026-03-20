package com.luscadevs.journeyorchestrator.adapters.config;

import java.util.List;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.adapters.out.memory.InMemoryJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import jakarta.annotation.PostConstruct;

@Component
public class JourneyDefinitionLoader {
    private final InMemoryJourneyDefinitionRepository repository;

    public JourneyDefinitionLoader(InMemoryJourneyDefinitionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void loadDefinitions() {
        State analisys = State.builder().name("ANALYSIS").initial(true).build();
        State approved = State.builder().name("APPROVED").finalState(true).build();
        State rejected = State.builder().name("REJECTED").finalState(true).build();

        Transition approve = Transition.builder().sourceState(analisys).targetState(approved)
                .event(Event.builder().name("APPROVE").description("Approve credit proposal").build()).build();

        Transition reject = Transition.builder().sourceState(analisys).targetState(rejected)
                .event(Event.builder().name("REJECT").description("Reject credit proposal").build()).build();

        JourneyDefinition definition = JourneyDefinition.builder().id("CREDIT_FLOW").version(1).initialState(analisys)
                .transitions(List.of(approve, reject)).build();

        repository.register(definition);
    }
}
