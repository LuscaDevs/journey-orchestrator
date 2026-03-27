# Quick Start Guide: MongoDB Persistence

**Feature**: 002-mongodb-persistence  
**Date**: 2026-03-26  

## Overview

This guide helps you set up and configure MongoDB persistence for the Journey Orchestrator application. The MongoDB persistence layer provides production-ready data storage while maintaining the existing hexagonal architecture.

## Prerequisites

- Java 21 or later
- MongoDB 6.0 or later
- Maven 3.8 or later
- Spring Boot 4.0.3

## Setup Instructions

### 1. MongoDB Installation

#### Local Development
```bash
# Using Docker (recommended)
docker run --name mongodb -p 27017:27017 -d mongo:6.0

# Or install MongoDB locally
# Follow MongoDB installation guide for your platform
```

#### Production Setup
- Configure MongoDB cluster with replica sets
- Enable authentication and authorization
- Set up proper backup and monitoring
- Configure network security

### 2. Application Configuration

Add MongoDB dependencies to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
</dependency>
```

Configure MongoDB connection in `application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/journey-orchestrator}
      database: ${MONGODB_DATABASE:journey-orchestrator}
      auto-index-creation: true

# Custom MongoDB configuration
journey-orchestrator:
  persistence:
    type: mongodb  # Options: memory, mongodb
    mongodb:
      connection-timeout: 5000
      max-pool-size: 20
      min-pool-size: 5
```

### 3. Environment-Specific Configuration

#### Development (`application-dev.yml`)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/journey-orchestrator-dev
```

#### Staging (`application-staging.yml`)
```yaml
spring:
  data:
    mongodb:
      uri: ${STAGING_MONGODB_URI}
```

#### Production (`application-prod.yml`)
```yaml
spring:
  data:
    mongodb:
      uri: ${PROD_MONGODB_URI}
      auto-index-creation: false
```

### 4. Running the Application

#### With MongoDB (Default)
```bash
mvn spring-boot:run
```

#### With In-Memory Persistence (Testing)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--journey-orchestrator.persistence.type=memory"
```

#### Using Spring Profiles
```bash
# Development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Configuration Options

### MongoDB Connection Settings

| Property | Default | Description |
|----------|---------|-------------|
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/journey-orchestrator` | MongoDB connection URI |
| `spring.data.mongodb.database` | `journey-orchestrator` | Database name |
| `spring.data.mongodb.auto-index-creation` | `true` | Auto-create indexes |

### Custom MongoDB Settings

| Property | Default | Description |
|----------|---------|-------------|
| `journey-orchestrator.persistence.mongodb.connection-timeout` | `5000` | Connection timeout (ms) |
| `journey-orchestrator.persistence.mongodb.max-pool-size` | `20` | Maximum connection pool size |
| `journey-orchestrator.persistence.mongodb.min-pool-size` | `5` | Minimum connection pool size |

## Monitoring and Maintenance

### 1. MongoDB Monitoring
- Monitor connection pool usage
- Track query performance
- Monitor disk space usage
- Set up alerts for connection failures

### 2. Application Monitoring
- Use Spring Boot Actuator endpoints
- Monitor MongoDB connection health
- Track persistence operation metrics
- Set up logging for errors

### 3. Backup Strategy
- Regular MongoDB backups
- Point-in-time recovery capability
- Test backup restoration procedures
- Document backup retention policies

## Troubleshooting

### Common Issues

#### Connection Refused
```bash
# Check MongoDB is running
docker ps | grep mongodb

# Check connection string
echo $MONGODB_URI
```

#### Authentication Failed
```bash
# Verify MongoDB credentials
mongo -u username -p password --authenticationDatabase admin
```

#### Performance Issues
```bash
# Check MongoDB indexes
mongo journey-orchestrator
db.journey_definitions.getIndexes()
db.journey_instances.getIndexes()
```

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    org.springframework.data.mongodb: DEBUG
    com.mongodb: DEBUG
```

## Production Deployment

### 1. MongoDB Cluster Setup
- Configure replica sets for high availability
- Enable authentication and SSL
- Set up proper monitoring and alerting
- Configure backup and recovery

### 2. Application Configuration
- Use environment variables for sensitive data
- Configure connection pooling appropriately
- Enable health checks and monitoring
- Set up proper logging and error handling

### 3. Security Considerations
- Use strong authentication credentials
- Enable network encryption (TLS/SSL)
- Configure firewall rules
- Regular security updates

## Next Steps

1. Configure MongoDB for your environment
2. Test the application with MongoDB persistence
3. Set up monitoring and alerting
4. Plan production deployment strategy
5. Document your specific configuration choices

## Support

For issues related to MongoDB persistence:
1. Check the application logs for error messages
2. Verify MongoDB connectivity and configuration
3. Review the troubleshooting section above
4. Consult the MongoDB documentation for specific issues
