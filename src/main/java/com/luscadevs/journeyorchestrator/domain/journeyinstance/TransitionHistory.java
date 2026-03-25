package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.Map;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransitionHistory {
    private State fromState;
    private State toState;
    private Event event;
    private Instant timestamp;
    private Map<String, Object> metadata;
}
