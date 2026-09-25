package com.luscadevs.journeyorchestrator.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.luscadevs.journeyorchestrator.api.mapper.ConnectorExecutionMapper;

/**
 * Configuração de teste para fornecer beans necessários nos testes.
 */
@TestConfiguration
@Import(ConnectorExecutionMapper.class)
public class TestConfig {
}
