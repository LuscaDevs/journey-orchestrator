package com.luscadevs.journeyorchestrator.adapters.in.web;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.luscadevs.journeyorchestrator.api.dto.connector.ConnectorExecutionResponse;
import com.luscadevs.journeyorchestrator.api.mapper.ConnectorExecutionMapper;
import com.luscadevs.journeyorchestrator.application.port.in.ConnectorPort;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/journey-instances")
@RequiredArgsConstructor
public class ConnectorExecutionController {

    private final ConnectorPort connectorPort;
    private final ConnectorExecutionMapper connectorExecutionMapper;

    @GetMapping("/{journeyInstanceId}/connector-executions")
    public ResponseEntity<List<ConnectorExecutionResponse>> getConnectorExecutionHistory(
            @PathVariable String journeyInstanceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        ConnectorExecutionRecord.ExecutionStatus executionStatus = null;
        if (status != null && !status.isBlank()) {
            executionStatus = ConnectorExecutionRecord.ExecutionStatus.valueOf(status.toUpperCase());
        }

        List<ConnectorExecutionRecord> records = connectorPort.getExecutionHistoryWithFilters(
                journeyInstanceId, from, to, executionStatus, limit, offset);

        return ResponseEntity.ok(connectorExecutionMapper.toResponseList(records));
    }

    @GetMapping("/{journeyInstanceId}/connector-executions/count")
    public ResponseEntity<Long> getConnectorExecutionCount(
            @PathVariable String journeyInstanceId) {
        return ResponseEntity.ok(connectorPort.countExecutions(journeyInstanceId));
    }
}
