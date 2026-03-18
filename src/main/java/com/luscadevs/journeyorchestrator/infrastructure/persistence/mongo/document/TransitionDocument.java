package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document;

import lombok.Data;

@Data
public class TransitionDocument {

    private String source;

    private String event;

    private String target;

}