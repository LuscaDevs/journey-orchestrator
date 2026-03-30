# Quickstart Guide: Conditional Transitions in Journey State Machine

**Feature**: `006-conditional-transitions`  
**Date**: 2025-03-30  
**Status**: Complete

## Overview

This guide provides a quick introduction to using conditional transitions in the journey orchestration system. Conditional transitions allow journeys to make routing decisions based on runtime context data while maintaining business-agnostic orchestration.

## Prerequisites

- Java 21 or higher
- Spring Boot 4.0.3 or higher
- MongoDB for persistence
- Basic understanding of journey orchestration concepts
- Familiarity with Spring Expression Language (SpEL)

## Getting Started

### 1. Define a Journey with Conditional Transitions

Create a journey definition with conditional transitions using the REST API:

```bash
curl -X POST http://localhost:8080/api/v1/journey-definitions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Order Processing Journey",
    "description": "Journey for processing customer orders with conditional routing",
    "version": "1.0.0",
    "states": [
      {
        "id": "START",
        "name": "Order Received",
        "type": "START"
      },
      {
        "id": "REVIEW",
        "name": "Order Review",
        "type": "INTERMEDIATE"
      },
      {
        "id": "FAST_TRACK",
        "name": "Fast Track Processing",
        "type": "INTERMEDIATE"
      },
      {
        "id": "COMPLETED",
        "name": "Order Completed",
        "type": "END"
      }
    ],
    "transitions": [
      {
        "id": "to_review",
        "fromState": "START",
        "toState": "REVIEW",
        "event": "SUBMIT",
        "condition": {
          "expression": "context.journeyData.amount > 1000 OR context.eventData.priority == 'HIGH'",
          "description": "Route to review for high-value or high-priority orders"
        },
        "conditionOrder": 1
      },
      {
        "id": "to_fast_track",
        "fromState": "START",
        "toState": "FAST_TRACK",
        "event": "SUBMIT",
        "condition": {
          "expression": "context.journeyData.amount <= 1000 AND context.eventData.priority == 'NORMAL'",
          "description": "Route to fast track for standard orders"
        },
        "conditionOrder": 2
      },
      {
        "id": "to_completed_from_review",
        "fromState": "REVIEW",
        "toState": "COMPLETED",
        "event": "APPROVE"
      },
      {
        "id": "to_completed_from_fast_track",
        "fromState": "FAST_TRACK",
        "toState": "COMPLETED",
        "event": "PROCESS"
      }
    ]
  }'
```

### 2. Validate Condition Expressions

Before using conditions in production, validate them:

```bash
curl -X POST http://localhost:8080/api/v1/conditions/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "expression": "context.journeyData.amount > 1000 OR context.eventData.priority == '\''HIGH'\''",
    "contextSample": {
      "journeyData": {
        "amount": 1500,
        "customerType": "PREMIUM"
      },
      "eventData": {
        "priority": "HIGH",
        "source": "WEB"
      },
      "systemData": {
        "timestamp": "2025-03-30T10:00:00Z",
        "correlationId": "abc123"
      }
    }
  }'
```

**Response**:
```json
{
  "valid": true,
  "errors": [],
  "warnings": [],
  "complexityScore": 3,
  "referencedProperties": [
    "context.journeyData.amount",
    "context.eventData.priority"
  ]
}
```

### 3. Test Condition Evaluation

Test conditions with sample context data:

```bash
curl -X POST http://localhost:8080/api/v1/conditions/test \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "expression": "context.journeyData.amount > 1000 AND context.eventData.priority == '\''HIGH'\''",
    "contextData": {
      "journeyInstanceId": "journey-123",
      "currentState": "START",
      "journeyData": {
        "amount": 1500,
        "customerType": "PREMIUM"
      },
      "eventData": {
        "priority": "HIGH",
        "source": "WEB"
      },
      "systemData": {
        "timestamp": "2025-03-30T10:00:00Z",
        "correlationId": "abc123"
      }
    }
  }'
```

**Response**:
```json
{
  "result": true,
  "success": true,
  "executionTime": "PT0.003S",
  "evaluatedAt": "2025-03-30T10:00:00Z"
}
```

### 4. Start a Journey Instance

Start a journey instance with context data:

```bash
curl -X POST http://localhost:8080/api/v1/journey-instances \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "journeyDefinitionId": "journey-def-123",
    "initialContext": {
      "journeyData": {
        "amount": 1500,
        "customerType": "PREMIUM"
      }
    }
  }'
```

### 5. Apply Events to Trigger Conditional Transitions

```bash
curl -X POST http://localhost:8080/api/v1/journey-instances/{instanceId}/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "event": "SUBMIT",
    "eventData": {
      "priority": "HIGH",
      "source": "WEB"
    }
  }'
```

The system will evaluate the conditions and route to the appropriate state based on the context data.

## Expression Language Reference

### Available Context Data

Conditions have access to three types of context data:

1. **Journey Data**: `context.journeyData.*` - Data stored with the journey instance
2. **Event Data**: `context.eventData.*` - Data from the triggering event
3. **System Data**: `context.systemData.*` - System-level data (timestamp, correlationId, etc.)

### Supported Operators

#### Logical Operators
- `AND` - Logical AND
- `OR` - Logical OR  
- `NOT` - Logical NOT

#### Comparison Operators
- `==` - Equal to
- `!=` - Not equal to
- `>` - Greater than
- `<` - Less than
- `>=` - Greater than or equal to
- `<=` - Less than or equal to

### Expression Examples

#### Simple Conditions
```spel
context.journeyData.amount > 1000
context.eventData.priority == 'HIGH'
context.journeyData.customerType == 'PREMIUM'
```

#### Complex Conditions
```spel
context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH'
context.journeyData.customerType == 'PREMIUM' OR context.eventData.source == 'VIP'
NOT (context.journeyData.amount < 100 AND context.eventData.priority == 'LOW')
```

#### Nested Conditions
```spel
(context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH') OR 
(context.journeyData.customerType == 'PREMIUM' AND context.journeyData.amount > 500)
```

### Available Properties

#### Journey Data Properties
- `context.journeyData.amount` - Order amount (decimal)
- `context.journeyData.customerType` - Customer type (string)
- `context.journeyData.region` - Geographic region (string)
- `context.journeyData.productCategory` - Product category (string)

#### Event Data Properties
- `context.eventData.priority` - Event priority (string: HIGH, NORMAL, LOW)
- `context.eventData.source` - Event source (string: WEB, API, MOBILE)
- `context.eventData.userId` - User ID (string)
- `context.eventData.timestamp` - Event timestamp (datetime)

#### System Data Properties
- `context.systemData.timestamp` - Current timestamp (datetime)
- `context.systemData.correlationId` - Request correlation ID (string)
- `context.systemData.currentState` - Current journey state (string)

## Best Practices

### 1. Keep Expressions Simple

**Good**:
```spel
context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH'
```

**Avoid**:
```spel
(context.journeyData.amount > 1000 ? context.eventData.priority == 'HIGH' : context.journeyData.customerType == 'PREMIUM') AND context.systemData.timestamp > T(java.time.Instant).now().minusSeconds(3600)
```

### 2. Use Descriptive Condition Descriptions

Always provide clear descriptions for conditions:

```json
{
  "condition": {
    "expression": "context.journeyData.amount > 1000 OR context.eventData.priority == 'HIGH'",
    "description": "Route to review for high-value (>$1000) or high-priority orders"
  }
}
```

### 3. Validate Conditions Before Deployment

Always validate conditions before using them in production:

```bash
curl -X POST http://localhost:8080/api/v1/conditions/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "expression": "your_condition_expression",
    "contextSample": { ... }
  }'
```

### 4. Test with Realistic Context Data

Test conditions with context data that represents real scenarios:

```json
{
  "contextSample": {
    "journeyData": {
      "amount": 1500,
      "customerType": "PREMIUM",
      "region": "US"
    },
    "eventData": {
      "priority": "HIGH",
      "source": "WEB",
      "userId": "user-123"
    },
    "systemData": {
      "timestamp": "2025-03-30T10:00:00Z",
      "correlationId": "test-123"
    }
  }
}
```

### 5. Monitor Condition Evaluation Performance

Monitor condition evaluation to ensure they meet performance targets:

```bash
curl -X GET "http://localhost:8080/api/v1/journey-instances/{instanceId}/condition-evaluations?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Troubleshooting

### Common Issues

#### 1. Condition Always Evaluates to False

**Problem**: Condition expression has syntax errors or references non-existent properties.

**Solution**: Validate the condition expression:

```bash
curl -X POST http://localhost:8080/api/v1/conditions/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "expression": "your_condition_expression",
    "contextSample": { ... }
  }'
```

#### 2. Security Violation Errors

**Problem**: Condition attempts to access restricted methods or properties.

**Solution**: Use only allowed context properties and avoid method calls:

**Invalid**:
```spel
context.systemData.getClass().forName('java.lang.Runtime')
```

**Valid**:
```spel
context.journeyData.amount > 1000
```

#### 3. Performance Issues

**Problem**: Condition evaluation is slow.

**Solution**: 
- Simplify complex expressions
- Reduce nesting levels
- Use indexed properties in comparisons

#### 4. Type Mismatch Errors

**Problem**: Comparing incompatible types.

**Solution**: Ensure type compatibility in comparisons:

**Invalid**:
```spel
context.journeyData.amount == 'HIGH'  // Comparing decimal to string
```

**Valid**:
```spel
context.journeyData.amount > 1000  // Comparing decimal to number
```

### Error Codes and Solutions

| Error Code | Description | Solution |
|------------|-------------|----------|
| `CONDITION_INVALID_SYNTAX` | Expression has invalid syntax | Check SpEL syntax and quote strings properly |
| `CONDITION_SECURITY_VIOLATION` | Access to restricted resources | Use only allowed context properties |
| `CONDITION_PROPERTY_NOT_FOUND` | Referenced property doesn't exist | Verify property names and context structure |
| `CONDITION_TYPE_MISMATCH` | Type compatibility issues | Ensure compatible types in comparisons |

## Monitoring and Debugging

### 1. View Condition Evaluation History

```bash
curl -X GET "http://localhost:8080/api/v1/journey-instances/{instanceId}/condition-evaluations" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. Check Evaluation Performance

Look for `executionTime` in evaluation results to monitor performance:

```json
{
  "result": true,
  "success": true,
  "executionTime": "PT0.003S",
  "evaluatedAt": "2025-03-30T10:00:00Z"
}
```

### 3. Debug Failed Evaluations

Check error details in failed evaluations:

```json
{
  "result": false,
  "success": false,
  "error": {
    "type": "RUNTIME_ERROR",
    "message": "Property 'context.journeyData.invalidProp' not found"
  }
}
```

## Next Steps

1. **Explore Advanced Features**: Learn about complex condition expressions and nested logic
2. **Integration Testing**: Set up integration tests for your conditional transitions
3. **Performance Optimization**: Implement caching strategies for frequently used conditions
4. **Monitoring**: Set up monitoring and alerting for condition evaluation performance
5. **Security Review**: Review security policies for condition expressions

## Additional Resources

- [API Documentation](contracts/conditional-transitions-api.md)
- [Data Model Reference](data-model.md)
- [Research Findings](research.md)
- [Spring Expression Language Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions)
- [Journey Orchestration Guide](../../docs/journey-dsl.md)
