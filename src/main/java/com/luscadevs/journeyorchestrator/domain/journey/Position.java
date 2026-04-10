package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Position value object for visual editor integration. Represents x,y coordinates for graph
 * rendering.
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Position {

    private BigDecimal x;
    private BigDecimal y;
}
