package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document;

import lombok.Data;

@Data
public class StateDocument {

    private String name;

    private String type;

}