package com.luscadevs.journeyorchestrator.api.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.luscadevs.journeyorchestrator.adapters.in.web.ConnectorExecutionController;
import com.luscadevs.journeyorchestrator.application.port.in.ConnectorPort;
import com.luscadevs.journeyorchestrator.config.TestConfig;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;

@WebMvcTest(ConnectorExecutionController.class)
@Import(TestConfig.class)
class ConnectorExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConnectorPort connectorPort;

    @Test
    @DisplayName("should return connector execution history for a journey instance")
    void shouldReturnConnectorExecutionHistory() throws Exception {
        ConnectorExecutionRecord record = ConnectorExecutionRecord.success(
                "instance-1",
                "state-1",
                ConnectorType.HTTP,
                "{\"url\":\"https://example.com\"}",
                Instant.parse("2026-06-27T10:15:30Z"),
                Instant.parse("2026-06-27T10:15:31Z"),
                "{\"ok\":true}");

        when(connectorPort.getExecutionHistoryWithFilters("instance-1", null, null, null, 50, 0))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/journey-instances/instance-1/connector-executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(record.getId()))
                .andExpect(jsonPath("$[0].journeyInstanceId").value("instance-1"))
                .andExpect(jsonPath("$[0].stateId").value("state-1"))
                .andExpect(jsonPath("$[0].connectorType").value("HTTP"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));

        verify(connectorPort).getExecutionHistoryWithFilters("instance-1", null, null, null, 50, 0);
    }

    @Test
    @DisplayName("should return connector execution history with filters")
    void shouldReturnConnectorExecutionHistoryWithFilters() throws Exception {
        Instant from = Instant.parse("2026-06-27T00:00:00Z");
        Instant to = Instant.parse("2026-06-27T23:59:59Z");
        ConnectorExecutionRecord record = ConnectorExecutionRecord.success(
                "instance-1",
                "state-1",
                ConnectorType.HTTP,
                "{\"url\":\"https://example.com\"}",
                Instant.parse("2026-06-27T10:15:30Z"),
                Instant.parse("2026-06-27T10:15:31Z"),
                "{\"ok\":true}");

        when(connectorPort.getExecutionHistoryWithFilters(
                "instance-1", from, to, ConnectorExecutionRecord.ExecutionStatus.SUCCESS, 10, 5))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/journey-instances/instance-1/connector-executions")
                .param("from", "2026-06-27T00:00:00Z")
                .param("to", "2026-06-27T23:59:59Z")
                .param("status", "SUCCESS")
                .param("limit", "10")
                .param("offset", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(record.getId()))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));

        verify(connectorPort).getExecutionHistoryWithFilters(
                "instance-1", from, to, ConnectorExecutionRecord.ExecutionStatus.SUCCESS, 10, 5);
    }

    @Test
    @DisplayName("should return connector execution count")
    void shouldReturnConnectorExecutionCount() throws Exception {
        when(connectorPort.countExecutions("instance-1")).thenReturn(5L);

        mockMvc.perform(get("/journey-instances/instance-1/connector-executions/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(connectorPort).countExecutions("instance-1");
    }
}
