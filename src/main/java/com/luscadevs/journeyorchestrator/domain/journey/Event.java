package com.luscadevs.journeyorchestrator.domain.journey;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
public class Event {
    private String name;
    private String description;
    private Map<String, Object> metadata;

    public Event(String name) {
        this.name = name;
    }
}
