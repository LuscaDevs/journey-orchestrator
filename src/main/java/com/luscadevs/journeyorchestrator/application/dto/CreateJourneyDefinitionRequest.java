package com.luscadevs.journeyorchestrator.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateJourneyDefinitionRequest {

    private String journeyCode;

    private String version;

    private String initialState;

    private List<StateDTO> states;

    private List<TransitionDTO> transitions;
}