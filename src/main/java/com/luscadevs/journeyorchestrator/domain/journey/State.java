package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "name")
public class State {

    private String name;

    private StateType type;
}