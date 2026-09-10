package com.luscadevs.journeyorchestrator.api.controller;

import com.luscadevs.journeyorchestrator.adapters.in.web.ConnectorExecutionController;
import com.luscadevs.journeyorchestrator.application.port.in.ConnectorPort;
import com.luscadevs.journeyorchestrator.config.TestConfig;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        when(connectorPort.getExecutionHistory("instance-1")).thenReturn(List.of(record));

        mockMvc.perform(get("/journey-instances/instance-1/connectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(record.getId()))
                .andExpect(jsonPath("$[0].journeyInstanceId").value("instance-1"))
                .andExpect(jsonPath("$[0].stateId").value("state-1"))
                .andExpect(jsonPath("$[0].connectorType").value("HTTP"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));

        verify(connectorPort).getExecutionHistory("instance-1");
    }
}
