package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JourneyInstanceContextTest {

    private JourneyInstance journeyInstance;

    @BeforeEach
    void setUp() {
        journeyInstance = JourneyInstance.builder()
                .id("test-id")
                .journeyDefinitionId("test-definition")
                .journeyVersion(1)
                .context(new HashMap<>())
                .build();
    }

    @Test
    void mergeData_shouldMergeSimpleValues() {
        Map<String, Object> data = Map.of("key1", "value1", "key2", "value2");
        
        journeyInstance.mergeData(data);
        
        assertThat(journeyInstance.getContext()).containsEntry("key1", "value1");
        assertThat(journeyInstance.getContext()).containsEntry("key2", "value2");
    }

    @Test
    void mergeData_shouldDeepMergeNestedMaps() {
        Map<String, Object> existingContext = new HashMap<>();
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("name", "John");
        customerData.put("age", 30);
        existingContext.put("customer", customerData);
        journeyInstance = JourneyInstance.builder()
                .id("test-id")
                .journeyDefinitionId("test-definition")
                .journeyVersion(1)
                .context(existingContext)
                .build();
        
        Map<String, Object> newData = new HashMap<>();
        Map<String, Object> newCustomerData = new HashMap<>();
        newCustomerData.put("name", "Jane");
        newCustomerData.put("email", "jane@example.com");
        newData.put("customer", newCustomerData);
        
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", "123");
        newData.put("order", orderData);
        
        journeyInstance.mergeData(newData);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) journeyInstance.getContext().get("customer");
        assertThat(customer).containsEntry("name", "Jane");
        assertThat(customer).containsEntry("age", 30);
        assertThat(customer).containsEntry("email", "jane@example.com");
        assertThat(journeyInstance.getContext()).containsKey("order");
    }

    @Test
    void mergeData_shouldHandleNullInput() {
        journeyInstance.mergeData(null);
        journeyInstance.mergeData(Map.of());
        
        assertThat(journeyInstance.getContext()).isEmpty();
    }

    @Test
    void mergeData_shouldHandleEmptyContext() {
        journeyInstance = JourneyInstance.builder()
                .id("test-id")
                .journeyDefinitionId("test-definition")
                .journeyVersion(1)
                .context(null)
                .build();
        
        Map<String, Object> data = Map.of("key", "value");
        journeyInstance.mergeData(data);
        
        assertThat(journeyInstance.getContext()).isNotNull();
        assertThat(journeyInstance.getContext()).containsEntry("key", "value");
    }

    @Test
    void getVariable_shouldReturnSimpleValue() {
        journeyInstance.getContext().put("key", "value");
        
        Object result = journeyInstance.getVariable("key");
        
        assertThat(result).isEqualTo("value");
    }

    @Test
    void getVariable_shouldReturnNestedValueWithDotNotation() {
        Map<String, Object> customer = new HashMap<>();
        customer.put("id", "123");
        customer.put("name", "John");
        journeyInstance.getContext().put("customer", customer);
        
        Object result = journeyInstance.getVariable("customer.id");
        
        assertThat(result).isEqualTo("123");
    }

    @Test
    void getVariable_shouldReturnNullForMissingKey() {
        Object result = journeyInstance.getVariable("nonexistent");
        
        assertThat(result).isNull();
    }

    @Test
    void getVariable_shouldReturnNullForMissingNestedKey() {
        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "John");
        journeyInstance.getContext().put("customer", customer);
        
        Object result = journeyInstance.getVariable("customer.nonexistent");
        
        assertThat(result).isNull();
    }

    @Test
    void getVariable_shouldHandleNullContext() {
        journeyInstance = JourneyInstance.builder()
                .id("test-id")
                .journeyDefinitionId("test-definition")
                .journeyVersion(1)
                .context(null)
                .build();
        
        Object result = journeyInstance.getVariable("key");
        
        assertThat(result).isNull();
    }

    @Test
    void getVariable_shouldHandleEmptyKey() {
        Object result = journeyInstance.getVariable("");
        
        assertThat(result).isNull();
    }

    @Test
    void hasVariable_shouldReturnTrueForExistingKey() {
        journeyInstance.getContext().put("key", "value");
        
        boolean result = journeyInstance.hasVariable("key");
        
        assertThat(result).isTrue();
    }

    @Test
    void hasVariable_shouldReturnTrueForExistingNestedKey() {
        Map<String, Object> customer = new HashMap<>();
        customer.put("id", "123");
        journeyInstance.getContext().put("customer", customer);
        
        boolean result = journeyInstance.hasVariable("customer.id");
        
        assertThat(result).isTrue();
    }

    @Test
    void hasVariable_shouldReturnFalseForMissingKey() {
        boolean result = journeyInstance.hasVariable("nonexistent");
        
        assertThat(result).isFalse();
    }

    @Test
    void hasVariable_shouldReturnFalseForMissingNestedKey() {
        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "John");
        journeyInstance.getContext().put("customer", customer);
        
        boolean result = journeyInstance.hasVariable("customer.nonexistent");
        
        assertThat(result).isFalse();
    }
}
