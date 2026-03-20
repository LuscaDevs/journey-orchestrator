package com.luscadevs.journeyorchestrator.application.port.in;

import java.util.Map;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public interface JourneyInstanceUseCase {

    JourneyInstance startJourney(String journeyCode, Integer version, Map<String, Object> payload);

    JourneyInstance applyEvent(String instanceId, Event event);

    JourneyInstance getInstance(String instanceId);

}