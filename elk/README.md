# ELK Stack Configuration

This directory contains the configuration files for the ELK (Elasticsearch, Logstash, Kibana) stack used for centralized logging in the Bookstore application.

## Directory Structure

```
elk/
├── filebeat/
│   └── filebeat.yml          # Filebeat configuration for log shipping
├── logstash/
│   ├── config/
│   │   └── logstash.yml      # Logstash main configuration
│   └── pipeline/
│       └── logstash.conf     # Logstash pipeline configuration
├── kibana/
│   └── dashboards/
│       └── bookstore-dashboard.ndjson  # Pre-configured Kibana dashboards
├── setup-elk.sh              # Setup script for Linux/Mac
├── setup-elk.ps1             # Setup script for Windows
└── README.md                 # This file
```

## Quick Start

### Linux/Mac

```bash
# Make the script executable
chmod +x elk/setup-elk.sh

# Run the setup script
./elk/setup-elk.sh
```

### Windows

```powershell
# Run the PowerShell script
.\elk\setup-elk.ps1
```

### Manual Setup

```bash
# Start ELK stack
docker-compose -f docker-compose-elk.yml up -d

# Check status
docker-compose -f docker-compose-elk.yml ps

# View logs
docker-compose -f docker-compose-elk.yml logs -f
```

## Configuration Files

### Filebeat (`filebeat/filebeat.yml`)

Monitors log files from all microservices and ships them to Logstash.

**Key Features:**
- Monitors multiple service log directories
- Adds service metadata to each log entry
- Handles multiline log entries (stack traces)
- Parses JSON log format

### Logstash (`logstash/pipeline/logstash.conf`)

Processes and enriches log data before sending to Elasticsearch.

**Pipeline Stages:**
1. **Input**: Receives logs from Filebeat (port 5044) and TCP (port 5000)
2. **Filter**: Parses JSON, extracts metadata, adds timestamps
3. **Output**: Sends to Elasticsearch with daily indices

### Kibana Dashboards

Pre-configured dashboards for visualizing logs:
- Service overview
- Error tracking
- Performance metrics
- Request tracing

## Accessing Services

Once started, access the services at:

- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Logstash**: http://localhost:9600

## Creating Index Pattern in Kibana

1. Open Kibana at http://localhost:5601
2. Navigate to **Management** → **Stack Management** → **Index Patterns**
3. Click **Create index pattern**
4. Enter pattern: `bookstore-logs-*`
5. Select `@timestamp` as the time field
6. Click **Create index pattern**

## Common Operations

### View Logs

```bash
# All services
docker-compose -f docker-compose-elk.yml logs -f

# Specific service
docker-compose -f docker-compose-elk.yml logs -f elasticsearch
docker-compose -f docker-compose-elk.yml logs -f logstash
docker-compose -f docker-compose-elk.yml logs -f kibana
docker-compose -f docker-compose-elk.yml logs -f filebeat
```

### Restart Services

```bash
# Restart all
docker-compose -f docker-compose-elk.yml restart

# Restart specific service
docker-compose -f docker-compose-elk.yml restart logstash
```

### Stop Services

```bash
# Stop all services
docker-compose -f docker-compose-elk.yml down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose -f docker-compose-elk.yml down -v
```

### Check Health

```bash
# Elasticsearch
curl http://localhost:9200/_cluster/health?pretty

# Logstash
curl http://localhost:9600/_node/stats?pretty

# Kibana
curl http://localhost:5601/api/status
```

## Troubleshooting

### Elasticsearch Won't Start

**Problem**: Elasticsearch container exits immediately

**Solutions**:
1. Increase Docker memory to at least 4GB
2. Check available disk space
3. Review logs: `docker-compose -f docker-compose-elk.yml logs elasticsearch`

### Logs Not Appearing

**Problem**: Services are running but logs don't appear in Kibana

**Solutions**:
1. Verify log files exist: `ls -la backend/*/logs/`
2. Check Filebeat is running: `docker-compose -f docker-compose-elk.yml ps filebeat`
3. Verify Logstash is processing: `curl http://localhost:9600/_node/stats`
4. Check Elasticsearch indices: `curl http://localhost:9200/_cat/indices?v`

### Permission Issues

**Problem**: Filebeat can't read log files

**Solution**:
```bash
# Linux/Mac
chmod -R 755 backend/*/logs/

# Windows - run as Administrator
icacls backend\*\logs /grant Everyone:F /T
```

### High Memory Usage

**Problem**: ELK stack consuming too much memory

**Solutions**:
1. Reduce heap sizes in `docker-compose-elk.yml`
2. Limit log retention
3. Delete old indices: `curl -X DELETE http://localhost:9200/bookstore-logs-2024.01.*`

## Configuration Customization

### Change Elasticsearch Heap Size

Edit `docker-compose-elk.yml`:

```yaml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms512m -Xmx512m"  # Adjust as needed
```

### Change Logstash Heap Size

Edit `docker-compose-elk.yml`:

```yaml
logstash:
  environment:
    - "LS_JAVA_OPTS=-Xmx256m -Xms256m"  # Adjust as needed
```

### Add Custom Logstash Filters

Edit `elk/logstash/pipeline/logstash.conf` and add filters in the `filter` section.

### Modify Filebeat Inputs

Edit `elk/filebeat/filebeat.yml` to add or modify log file paths.

## Index Management

### View All Indices

```bash
curl http://localhost:9200/_cat/indices?v
```

### Delete Old Indices

```bash
# Delete specific index
curl -X DELETE http://localhost:9200/bookstore-logs-2024.01.01

# Delete indices matching pattern
curl -X DELETE http://localhost:9200/bookstore-logs-2024.01.*
```

### Create Index Lifecycle Policy

```bash
curl -X PUT "http://localhost:9200/_ilm/policy/bookstore-logs-policy" \
  -H 'Content-Type: application/json' \
  -d '{
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

## Performance Tuning

### For Development

- Elasticsearch: 512MB-1GB heap
- Logstash: 256MB-512MB heap
- Keep 7-14 days of logs

### For Production

- Elasticsearch: 2GB-4GB heap (or more)
- Logstash: 1GB-2GB heap
- Implement index lifecycle management
- Use multiple Elasticsearch nodes
- Enable security features

## Security Considerations

For production deployments:

1. Enable Elasticsearch security (X-Pack)
2. Use HTTPS for all connections
3. Implement authentication and authorization
4. Encrypt sensitive log data
5. Restrict network access
6. Regular security updates

## Additional Resources

- [Complete ELK Logging Guide](../ELK_LOGGING_GUIDE.md)
- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Logstash Documentation](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Kibana Documentation](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Filebeat Documentation](https://www.elastic.co/guide/en/beats/filebeat/current/index.html)

## Support

For issues or questions:
1. Check the [ELK Logging Guide](../ELK_LOGGING_GUIDE.md)
2. Review service logs
3. Consult Elastic documentation
4. Contact the development team