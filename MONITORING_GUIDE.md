# Monitoring Guide - Prometheus & Grafana Integration

This guide explains how to set up and use Prometheus and Grafana for monitoring the Book Management System microservices.

## Overview

The monitoring stack includes:
- **Prometheus**: Metrics collection and storage
- **Grafana**: Visualization and dashboards
- **Micrometer**: Metrics instrumentation in Spring Boot applications

## Architecture

```
┌─────────────────┐
│  Microservices  │
│  (Spring Boot)  │
│                 │
│  /actuator/     │
│  prometheus     │
└────────┬────────┘
         │ Scrapes metrics
         │ every 15s
         ▼
┌─────────────────┐
│   Prometheus    │
│   (Port 9090)   │
│                 │
│  Time-series DB │
└────────┬────────┘
         │ Queries
         │ metrics
         ▼
┌─────────────────┐
│    Grafana      │
│   (Port 3000)   │
│                 │
│   Dashboards    │
└─────────────────┘
```

## Prerequisites

- Docker and Docker Compose installed
- All microservices running locally
- Services accessible on their configured ports:
  - Book Service: 8081
  - Inventory Service: 8082
  - Notification Service: 8083
  - Cart Service: 8084
  - Auth Service: 8086

## Quick Start

### 1. Start Monitoring Stack

```bash
# Start Prometheus and Grafana
docker-compose -f docker-compose-monitoring.yml up -d

# Check status
docker-compose -f docker-compose-monitoring.yml ps

# View logs
docker-compose -f docker-compose-monitoring.yml logs -f
```

### 2. Access Monitoring Tools

**Prometheus UI:**
- URL: http://localhost:9090
- Features:
  - Query metrics using PromQL
  - View targets and their health status
  - Explore available metrics

**Grafana UI:**
- URL: http://localhost:3000
- Default credentials:
  - Username: `admin`
  - Password: `admin`
- Change password on first login

### 3. Verify Metrics Collection

**Check Prometheus Targets:**
1. Open http://localhost:9090/targets
2. Verify all services show as "UP"
3. Check last scrape time and duration

**Expected Targets:**
- prometheus (self-monitoring)
- book-service
- auth-service
- cart-service
- inventory-service
- notification-service

## Available Metrics

### HTTP Metrics
- `http_server_requests_seconds_count`: Total HTTP requests
- `http_server_requests_seconds_sum`: Total request duration
- `http_server_requests_seconds_max`: Maximum request duration

### JVM Metrics
- `jvm_memory_used_bytes`: JVM memory usage
- `jvm_memory_max_bytes`: Maximum JVM memory
- `jvm_threads_live_threads`: Number of live threads
- `jvm_gc_pause_seconds`: Garbage collection pause time

### System Metrics
- `system_cpu_usage`: System CPU usage
- `process_cpu_usage`: Process CPU usage
- `system_load_average_1m`: System load average

### Database Metrics
- `hikaricp_connections_active`: Active database connections
- `hikaricp_connections_idle`: Idle database connections
- `hikaricp_connections_pending`: Pending connection requests

### Custom Application Metrics
Each service exposes application-specific metrics through the `/actuator/prometheus` endpoint.

## Grafana Dashboards

### Pre-configured Dashboard

The system includes a pre-configured "Spring Boot Microservices Dashboard" with:

1. **HTTP Request Rate**: Requests per second by service and endpoint
2. **Average Response Time**: Response time gauge by service
3. **JVM Memory Usage**: Heap and non-heap memory usage
4. **CPU Usage**: System and process CPU utilization
5. **Database Connection Pool**: Active and idle connections
6. **JVM Threads**: Thread count over time

### Accessing the Dashboard

1. Login to Grafana (http://localhost:3000)
2. Navigate to Dashboards → Browse
3. Open "Spring Boot Microservices Dashboard"
4. Use the "Application" dropdown to filter by service

### Creating Custom Dashboards

1. Click "+" → Dashboard
2. Add Panel
3. Select Prometheus as data source
4. Write PromQL queries
5. Configure visualization
6. Save dashboard

### Example PromQL Queries

**Request rate per service:**
```promql
rate(http_server_requests_seconds_count{application="book-service"}[1m])
```

**Average response time:**
```promql
http_server_requests_seconds_sum / http_server_requests_seconds_count
```

**Memory usage percentage:**
```promql
(jvm_memory_used_bytes / jvm_memory_max_bytes) * 100
```

**Error rate:**
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

## Monitoring Best Practices

### 1. Set Up Alerts

Create alert rules in Prometheus for:
- High error rates (>5%)
- Slow response times (>1s)
- High memory usage (>80%)
- High CPU usage (>80%)
- Database connection pool exhaustion

### 2. Monitor Key Metrics

Focus on:
- **RED Metrics**: Rate, Errors, Duration
- **USE Metrics**: Utilization, Saturation, Errors
- Business metrics (books created, orders processed, etc.)

### 3. Dashboard Organization

- Create separate dashboards for different concerns
- Use variables for dynamic filtering
- Set appropriate time ranges
- Add annotations for deployments

### 4. Retention and Storage

Default Prometheus retention: 15 days
To change retention:
```yaml
command:
  - '--storage.tsdb.retention.time=30d'
```

## Troubleshooting

### Services Not Showing in Prometheus

1. **Check service is running:**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

2. **Verify Prometheus endpoint:**
   ```bash
   curl http://localhost:8081/actuator/prometheus
   ```

3. **Check Prometheus configuration:**
   ```bash
   docker exec prometheus cat /etc/prometheus/prometheus.yml
   ```

4. **View Prometheus logs:**
   ```bash
   docker logs prometheus
   ```

### Grafana Not Showing Data

1. **Verify Prometheus data source:**
   - Configuration → Data Sources → Prometheus
   - Test connection

2. **Check time range:**
   - Ensure time range includes data
   - Try "Last 15 minutes"

3. **Verify metrics exist:**
   - Go to Explore
   - Select Prometheus
   - Browse available metrics

### Connection Issues

If using `host.docker.internal` doesn't work:
1. Find your host IP: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
2. Update `prometheus.yml` with actual IP address
3. Restart Prometheus

## Advanced Configuration

### Custom Metrics

Add custom metrics in your Spring Boot services:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
public class BookService {
    private final Counter bookCreatedCounter;
    
    public BookService(MeterRegistry registry) {
        this.bookCreatedCounter = Counter.builder("books.created")
            .description("Number of books created")
            .tag("service", "book-service")
            .register(registry);
    }
    
    public Book createBook(Book book) {
        // ... create book logic
        bookCreatedCounter.increment();
        return book;
    }
}
```

### Adding More Scrape Targets

Edit `monitoring/prometheus/prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'new-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8087']
        labels:
          application: 'new-service'
```

Reload Prometheus configuration:
```bash
curl -X POST http://localhost:9090/-/reload
```

### Grafana Plugins

Install additional plugins:
```yaml
environment:
  - GF_INSTALL_PLUGINS=grafana-piechart-panel,grafana-clock-panel
```

## Stopping Monitoring Stack

```bash
# Stop containers
docker-compose -f docker-compose-monitoring.yml down

# Stop and remove volumes (deletes all data)
docker-compose -f docker-compose-monitoring.yml down -v
```

## Security Considerations

### Production Deployment

1. **Change default passwords:**
   ```yaml
   environment:
     - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
   ```

2. **Enable authentication in Prometheus:**
   - Use reverse proxy with authentication
   - Implement network policies

3. **Use HTTPS:**
   - Configure TLS certificates
   - Enable HTTPS in Grafana

4. **Restrict access:**
   - Use firewall rules
   - Implement IP whitelisting
   - Use VPN for remote access

## Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## Support

For issues or questions:
1. Check service logs: `docker-compose -f docker-compose-monitoring.yml logs [service]`
2. Verify configuration files
3. Review Prometheus targets: http://localhost:9090/targets
4. Check Grafana data source connection

---

**Note**: This monitoring setup is configured for development. For production use, implement proper security measures, backup strategies, and high availability configurations.