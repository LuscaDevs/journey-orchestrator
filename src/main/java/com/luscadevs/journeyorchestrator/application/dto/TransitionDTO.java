package com.luscadevs.journeyorchestrator.application.dto;

import lombok.Data;

@Data
public class TransitionDTO {

    private String source;

    private String event;

    private String target;

}