# ELK Stack Logging Guide

This guide explains how to set up and use the ELK (Elasticsearch, Logstash, Kibana) stack for centralized logging in the Bookstore microservices application.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Setup Instructions](#setup-instructions)
5. [Configuration Details](#configuration-details)
6. [Using Kibana](#using-kibana)
7. [Log Format](#log-format)
8. [Troubleshooting](#troubleshooting)
9. [Best Practices](#best-practices)

## Overview

The ELK stack provides centralized logging for all microservices:

- **Elasticsearch**: Stores and indexes log data
- **Logstash**: Processes and transforms log data
- **Kibana**: Visualizes and searches logs
- **Filebeat**: Ships log files to Logstash

## Architecture

```
Microservices → Log Files (JSON) → Filebeat → Logstash → Elasticsearch → Kibana
                                                                            ↓
                                                                    Dashboards & Search
```

### Components

1. **Services**: Generate JSON-formatted logs using Logback
2. **Filebeat**: Monitors log files and ships them to Logstash
3. **Logstash**: Parses, filters, and enriches log data
4. **Elasticsearch**: Stores indexed log data
5. **Kibana**: Provides UI for searching and visualizing logs

## Prerequisites

- Docker and Docker Compose installed
- At least 4GB RAM available for ELK stack
- Services configured with Logback JSON logging

## Setup Instructions

### 1. Start the ELK Stack

```bash
# Start ELK services
docker-compose -f docker-compose-elk.yml up -d

# Check service health
docker-compose -f docker-compose-elk.yml ps

# View logs
docker-compose -f docker-compose-elk.yml logs -f
```

### 2. Verify Services

Wait for all services to be healthy (may take 2-3 minutes):

```bash
# Check Elasticsearch
curl http://localhost:9200/_cluster/health

# Check Logstash
curl http://localhost:9600/_node/stats

# Check Kibana
curl http://localhost:5601/api/status
```

### 3. Access Kibana

Open your browser and navigate to:
```
http://localhost:5601
```

### 4. Configure Index Pattern

1. Go to **Management** → **Stack Management** → **Index Patterns**
2. Click **Create index pattern**
3. Enter pattern: `bookstore-logs-*`
4. Select `@timestamp` as the time field
5. Click **Create index pattern**

### 5. Start Your Microservices

Ensure your services are configured with the Logback configuration and start them:

```bash
# Start all services
docker-compose up -d

# Or start individual services
cd backend/book-service
mvn spring-boot:run
```

## Configuration Details

### Logback Configuration

Each service should have `logback-spring.xml` in `src/main/resources/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console and File appenders with JSON encoding -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/${service-name}.log</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${service-name}"}</customFields>
        </encoder>
    </appender>
</configuration>
```

### Maven Dependency

Add to each service's `pom.xml`:

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Filebeat Configuration

Located at `elk/filebeat/filebeat.yml`:

- Monitors log directories for each service
- Adds service metadata
- Ships logs to Logstash on port 5044

### Logstash Pipeline

Located at `elk/logstash/pipeline/logstash.conf`:

- Receives logs from Filebeat
- Parses JSON format
- Enriches with metadata
- Outputs to Elasticsearch

## Using Kibana

### Discover Logs

1. Go to **Discover** in Kibana
2. Select the `bookstore-logs-*` index pattern
3. Use the search bar to filter logs:

```
# Search by service
service: "book-service"

# Search by log level
level: "ERROR"

# Search by message content
message: "exception"

# Combine filters
service: "book-service" AND level: "ERROR"

# Search by time range
@timestamp >= "2024-01-01" AND @timestamp <= "2024-01-31"
```

### Create Visualizations

1. Go to **Visualize Library**
2. Click **Create visualization**
3. Choose visualization type (e.g., Bar chart, Pie chart)
4. Configure metrics and buckets
5. Save the visualization

### Build Dashboards

1. Go to **Dashboard**
2. Click **Create dashboard**
3. Add saved visualizations
4. Arrange and resize panels
5. Save the dashboard

### Common Queries

**Find all errors:**
```
level: "ERROR"
```

**Find errors in specific service:**
```
service: "book-service" AND level: "ERROR"
```

**Find logs with exceptions:**
```
has_exception: "true"
```

**Find logs by correlation ID:**
```
correlation_id: "abc-123-def"
```

**Find slow requests (if logged):**
```
duration > 1000
```

## Log Format

### Standard Fields

All logs include these fields:

- `@timestamp`: Log timestamp (ISO 8601)
- `level`: Log level (DEBUG, INFO, WARN, ERROR)
- `service`: Service name
- `message`: Log message
- `logger_name`: Java class name
- `thread_name`: Thread name
- `environment`: Environment (development, staging, production)

### Custom Fields

Services can add custom fields:

- `correlation_id`: Request correlation ID
- `user_id`: User identifier
- `request_id`: HTTP request ID
- `duration`: Operation duration (ms)
- `exception`: Exception details (if present)

### Example Log Entry

```json
{
  "@timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "service": "book-service",
  "message": "Book created successfully",
  "logger_name": "com.bookstore.service.BookService",
  "thread_name": "http-nio-8080-exec-1",
  "environment": "development",
  "correlation_id": "abc-123-def",
  "user_id": "user@example.com",
  "book_id": 42,
  "duration": 150
}
```

## Troubleshooting

### Elasticsearch Not Starting

**Issue**: Elasticsearch fails to start or crashes

**Solutions**:
1. Increase Docker memory to at least 4GB
2. Check disk space
3. Disable security if not needed:
   ```yaml
   environment:
     - xpack.security.enabled=false
   ```

### Logs Not Appearing in Kibana

**Issue**: Services are running but logs don't appear

**Solutions**:
1. Check Filebeat is running:
   ```bash
   docker-compose -f docker-compose-elk.yml logs filebeat
   ```

2. Verify log files exist:
   ```bash
   ls -la backend/*/logs/
   ```

3. Check Logstash is processing:
   ```bash
   curl http://localhost:9600/_node/stats
   ```

4. Verify Elasticsearch indices:
   ```bash
   curl http://localhost:9200/_cat/indices?v
   ```

### Filebeat Permission Issues

**Issue**: Filebeat can't read log files

**Solution**: Ensure log directories have proper permissions:
```bash
chmod -R 755 backend/*/logs/
```

### High Memory Usage

**Issue**: ELK stack consuming too much memory

**Solutions**:
1. Reduce Elasticsearch heap size:
   ```yaml
   environment:
     - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
   ```

2. Reduce Logstash heap size:
   ```yaml
   environment:
     - "LS_JAVA_OPTS=-Xmx256m -Xms256m"
   ```

3. Limit log retention:
   ```bash
   # Delete old indices
   curl -X DELETE "localhost:9200/bookstore-logs-2024.01.*"
   ```

## Best Practices

### 1. Structured Logging

Always use structured logging with meaningful fields:

```java
log.info("Book created", 
    kv("bookId", book.getId()),
    kv("title", book.getTitle()),
    kv("duration", duration));
```

### 2. Log Levels

Use appropriate log levels:

- **DEBUG**: Detailed diagnostic information
- **INFO**: General informational messages
- **WARN**: Warning messages for potentially harmful situations
- **ERROR**: Error events that might still allow the application to continue

### 3. Correlation IDs

Always include correlation IDs for request tracing:

```java
MDC.put("correlation_id", correlationId);
log.info("Processing request");
MDC.remove("correlation_id");
```

### 4. Exception Logging

Log exceptions with full stack traces:

```java
try {
    // operation
} catch (Exception e) {
    log.error("Operation failed", e);
}
```

### 5. Performance Considerations

- Use async appenders for better performance
- Avoid logging sensitive data (passwords, tokens)
- Use appropriate log levels in production
- Implement log sampling for high-volume logs

### 6. Index Management

Set up index lifecycle management:

```bash
# Create ILM policy
curl -X PUT "localhost:9200/_ilm/policy/bookstore-logs-policy" -H 'Content-Type: application/json' -d'
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "7d"
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}'
```

### 7. Monitoring

Monitor ELK stack health:

- Set up alerts for disk space
- Monitor Elasticsearch cluster health
- Track log ingestion rate
- Monitor query performance

### 8. Security

For production environments:

1. Enable Elasticsearch security
2. Use HTTPS for Kibana
3. Implement authentication
4. Restrict network access
5. Encrypt sensitive log data

## Useful Commands

### Elasticsearch

```bash
# Check cluster health
curl http://localhost:9200/_cluster/health?pretty

# List all indices
curl http://localhost:9200/_cat/indices?v

# Get index stats
curl http://localhost:9200/bookstore-logs-*/_stats?pretty

# Delete old indices
curl -X DELETE http://localhost:9200/bookstore-logs-2024.01.01
```

### Logstash

```bash
# Check node stats
curl http://localhost:9600/_node/stats?pretty

# Check pipeline stats
curl http://localhost:9600/_node/stats/pipelines?pretty
```

### Docker

```bash
# View logs
docker-compose -f docker-compose-elk.yml logs -f elasticsearch
docker-compose -f docker-compose-elk.yml logs -f logstash
docker-compose -f docker-compose-elk.yml logs -f kibana
docker-compose -f docker-compose-elk.yml logs -f filebeat

# Restart services
docker-compose -f docker-compose-elk.yml restart

# Stop services
docker-compose -f docker-compose-elk.yml down

# Remove volumes (WARNING: deletes all data)
docker-compose -f docker-compose-elk.yml down -v
```

## Additional Resources

- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Logstash Documentation](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Kibana Documentation](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Filebeat Documentation](https://www.elastic.co/guide/en/beats/filebeat/current/index.html)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review service logs
3. Consult Elastic documentation
4. Contact the development team