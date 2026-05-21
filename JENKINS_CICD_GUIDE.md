# Jenkins CI/CD Guide

This guide explains how to set up and use Jenkins for continuous integration and continuous deployment (CI/CD) in the Bookstore microservices application.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Setup Instructions](#setup-instructions)
5. [Pipeline Stages](#pipeline-stages)
6. [Configuration](#configuration)
7. [Deployment Process](#deployment-process)
8. [Troubleshooting](#troubleshooting)
9. [Best Practices](#best-practices)

## Overview

The CI/CD pipeline automates the entire software delivery process from code commit to production deployment:

- **Continuous Integration**: Automated building and testing
- **Code Quality**: SonarQube analysis and quality gates
- **Security Scanning**: Docker image vulnerability scanning
- **Automated Deployment**: Staging and production deployments
- **Health Checks**: Post-deployment verification
- **Rollback**: Automatic rollback on failure

## Architecture

```
GitHub → Jenkins → Build → Test → Quality Gate → Docker Build → Push → Deploy → Verify
                     ↓        ↓         ↓            ↓          ↓      ↓       ↓
                   Maven   JUnit   SonarQube    Docker Hub   K8s/Docker  Smoke Tests
```

### Components

1. **Jenkins**: CI/CD orchestration
2. **SonarQube**: Code quality analysis
3. **Nexus**: Artifact repository
4. **Docker Registry**: Container image storage
5. **Deployment Scripts**: Automated deployment automation

## Prerequisites

- Docker and Docker Compose installed
- Git repository with code
- At least 8GB RAM for Jenkins stack
- Docker Hub account (or private registry)

## Setup Instructions

### 1. Start Jenkins Stack

```bash
# Start Jenkins and related services
docker-compose -f docker-compose-jenkins.yml up -d

# Check service status
docker-compose -f docker-compose-jenkins.yml ps

# View Jenkins logs
docker-compose -f docker-compose-jenkins.yml logs -f jenkins
```

### 2. Access Jenkins

Open your browser and navigate to:
```
http://localhost:8090
```

### 3. Initial Jenkins Setup

1. **Get Initial Admin Password**:
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

2. **Install Suggested Plugins**:
   - Git plugin
   - Pipeline plugin
   - Docker plugin
   - Blue Ocean (optional)
   - SonarQube Scanner
   - Email Extension Plugin

3. **Create Admin User**:
   - Username: admin
   - Password: (your secure password)
   - Email: admin@bookstore.com

### 4. Configure Jenkins

#### Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available**:

- Docker Pipeline
- Pipeline: Stage View
- Blue Ocean
- SonarQube Scanner
- Kubernetes (if using K8s)
- Email Extension Plugin
- Slack Notification (optional)

#### Configure Tools

Go to **Manage Jenkins** → **Global Tool Configuration**:

**Maven**:
- Name: `Maven-3.9`
- Install automatically: Yes
- Version: 3.9.x

**JDK**:
- Name: `JDK-17`
- Install automatically: Yes
- Version: Java 17

**Docker**:
- Name: `Docker`
- Install automatically: Yes

**SonarQube Scanner**:
- Name: `SonarQube Scanner`
- Install automatically: Yes

### 5. Configure Credentials

Go to **Manage Jenkins** → **Manage Credentials** → **Global**:

#### Docker Hub Credentials
- Kind: Username with password
- ID: `docker-hub-credentials`
- Username: (your Docker Hub username)
- Password: (your Docker Hub password/token)

#### GitHub Credentials
- Kind: Username with password (or SSH key)
- ID: `github-credentials`
- Username: (your GitHub username)
- Password: (your GitHub personal access token)

#### SonarQube Token
- Kind: Secret text
- ID: `sonarqube-token`
- Secret: (SonarQube authentication token)

### 6. Configure SonarQube

1. **Access SonarQube**:
   ```
   http://localhost:9000
   ```
   Default credentials: admin/admin

2. **Generate Token**:
   - Go to **My Account** → **Security** → **Generate Tokens**
   - Name: jenkins
   - Copy the token

3. **Configure in Jenkins**:
   - Go to **Manage Jenkins** → **Configure System**
   - Find **SonarQube servers**
   - Add SonarQube:
     - Name: `SonarQube`
     - Server URL: `http://sonarqube:9000`
     - Server authentication token: (select sonarqube-token credential)

### 7. Create Jenkins Pipeline

1. **New Item**:
   - Click **New Item**
   - Enter name: `bookstore-pipeline`
   - Select **Pipeline**
   - Click **OK**

2. **Configure Pipeline**:
   - **General**:
     - Description: "CI/CD pipeline for Bookstore microservices"
     - Discard old builds: Keep last 10 builds
   
   - **Build Triggers**:
     - ☑ GitHub hook trigger for GITScm polling
     - ☑ Poll SCM: `H/5 * * * *` (every 5 minutes)
   
   - **Pipeline**:
     - Definition: Pipeline script from SCM
     - SCM: Git
     - Repository URL: (your GitHub repo URL)
     - Credentials: (select github-credentials)
     - Branch: `*/main`
     - Script Path: `Jenkinsfile`

3. **Save** the configuration

### 8. Configure GitHub Webhook (Optional)

1. Go to your GitHub repository
2. Navigate to **Settings** → **Webhooks** → **Add webhook**
3. Payload URL: `http://your-jenkins-url:8090/github-webhook/`
4. Content type: `application/json`
5. Events: Just the push event
6. Active: ☑
7. Click **Add webhook**

## Pipeline Stages

### 1. Checkout
- Clones the repository
- Checks out the specified branch

### 2. Build Common Module
- Builds the shared common module
- Installs to local Maven repository

### 3. Build Services (Parallel)
- Builds all microservices in parallel
- Packages JAR files
- Skips tests for faster build

### 4. Unit Tests (Parallel)
- Runs unit tests for each service
- Generates test reports
- Publishes JUnit results

### 5. Code Quality Analysis
- Runs SonarQube analysis
- Checks code coverage
- Identifies code smells and bugs
- Only on main branch

### 6. Quality Gate
- Waits for SonarQube quality gate result
- Aborts pipeline if quality gate fails
- Only on main branch

### 7. Build Docker Images (Parallel)
- Builds Docker images for all services
- Tags with build number and 'latest'
- Uses multi-stage builds for optimization

### 8. Security Scan
- Scans Docker images for vulnerabilities
- Uses Docker scan or Trivy
- Continues even if vulnerabilities found (warning only)

### 9. Push Docker Images
- Pushes images to Docker registry
- Only on main and develop branches
- Tags with version and latest

### 10. Deploy to Staging
- Deploys to staging environment
- Only on develop branch
- Uses blue-green deployment

### 11. Deploy to Production
- Requires manual approval
- Only on main branch
- Uses rolling update strategy
- Creates backup before deployment

### 12. Health Check
- Verifies all services are healthy
- Checks actuator endpoints
- Runs after deployment

### 13. Smoke Tests
- Runs basic functionality tests
- Verifies critical paths
- Ensures deployment success

## Configuration

### Environment Variables

Set in Jenkinsfile or Jenkins configuration:

```groovy
environment {
    DOCKER_REGISTRY = 'docker.io'
    DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    SONAR_HOST_URL = 'http://sonarqube:9000'
    SONAR_PROJECT_KEY = 'bookstore-microservices'
}
```

### Service Ports

Default ports for services:

| Service | Port |
|---------|------|
| Auth Service | 8081 |
| Book Service | 8082 |
| Cart Service | 8083 |
| Inventory Service | 8084 |
| Notification Service | 8085 |

### Docker Images

Images are tagged with:
- Build number: `bookstore/service:123`
- Latest: `bookstore/service:latest`

## Deployment Process

### Staging Deployment

Triggered automatically on `develop` branch:

```bash
# Manual deployment
./scripts/deploy-staging.sh <version>

# Check health
./scripts/health-check.sh staging

# Run smoke tests
./scripts/smoke-tests.sh staging
```

### Production Deployment

Requires manual approval on `main` branch:

```bash
# Manual deployment
./scripts/deploy-production.sh <version>

# Check health
./scripts/health-check.sh production

# Run smoke tests
./scripts/smoke-tests.sh production
```

### Rollback

If deployment fails, automatic rollback is triggered:

```bash
# Manual rollback
docker-compose -f docker-compose-production.yml down
docker-compose -f docker-compose-production.yml up -d
```

## Troubleshooting

### Pipeline Fails at Build Stage

**Issue**: Maven build fails

**Solutions**:
1. Check Maven configuration in Jenkins
2. Verify Java version (should be 17)
3. Check pom.xml for errors
4. Review build logs: `docker-compose logs jenkins`

### Quality Gate Fails

**Issue**: SonarQube quality gate fails

**Solutions**:
1. Review SonarQube dashboard: http://localhost:9000
2. Fix code quality issues
3. Adjust quality gate thresholds if needed
4. Check code coverage requirements

### Docker Build Fails

**Issue**: Docker image build fails

**Solutions**:
1. Verify Dockerfile exists in service directory
2. Check Docker daemon is running
3. Ensure sufficient disk space
4. Review Docker build logs

### Deployment Fails

**Issue**: Deployment script fails

**Solutions**:
1. Check deployment logs
2. Verify target environment is accessible
3. Ensure Docker registry credentials are correct
4. Check service health endpoints

### Health Check Fails

**Issue**: Services not responding to health checks

**Solutions**:
1. Check service logs: `docker-compose logs <service>`
2. Verify database connectivity
3. Check Redis and Kafka connectivity
4. Ensure correct ports are exposed

## Best Practices

### 1. Branch Strategy

- **main**: Production-ready code
- **develop**: Integration branch
- **feature/***: Feature branches
- **hotfix/***: Emergency fixes

### 2. Commit Messages

Use conventional commits:
```
feat: add new feature
fix: bug fix
docs: documentation update
test: add tests
refactor: code refactoring
```

### 3. Version Tagging

Tag releases in Git:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 4. Code Quality

- Maintain >80% code coverage
- Fix all critical and blocker issues
- Keep technical debt low
- Regular code reviews

### 5. Security

- Scan dependencies regularly
- Update base images
- Use secrets management
- Implement least privilege access

### 6. Monitoring

- Monitor pipeline execution time
- Track deployment frequency
- Measure mean time to recovery (MTTR)
- Monitor failure rates

### 7. Documentation

- Keep Jenkinsfile documented
- Update deployment guides
- Document configuration changes
- Maintain runbooks

## Pipeline Metrics

Track these metrics:

- **Build Success Rate**: Target >95%
- **Build Duration**: Target <15 minutes
- **Deployment Frequency**: Daily for staging
- **Lead Time**: Commit to production <1 day
- **MTTR**: <1 hour
- **Change Failure Rate**: <5%

## Notifications

### Email Notifications

Configured in Jenkinsfile post section:
- Success: Sent to team
- Failure: Sent to team with logs
- Unstable: Warning notification

### Slack Integration (Optional)

Add Slack plugin and configure:
```groovy
slackSend(
    color: 'good',
    message: "Build ${env.BUILD_NUMBER} succeeded"
)
```

## Useful Commands

### Jenkins

```bash
# Restart Jenkins
docker-compose -f docker-compose-jenkins.yml restart jenkins

# View Jenkins logs
docker-compose -f docker-compose-jenkins.yml logs -f jenkins

# Backup Jenkins
docker exec jenkins tar czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp jenkins:/tmp/jenkins-backup.tar.gz ./jenkins-backup.tar.gz

# Restore Jenkins
docker cp ./jenkins-backup.tar.gz jenkins:/tmp/
docker exec jenkins tar xzf /tmp/jenkins-backup.tar.gz -C /
```

### SonarQube

```bash
# Access SonarQube
http://localhost:9000

# View SonarQube logs
docker-compose -f docker-compose-jenkins.yml logs -f sonarqube

# Reset SonarQube admin password
docker exec -it sonarqube-db psql -U sonar -d sonar
UPDATE users SET crypted_password='$2a$12$uCkkXmhW5ThVK8mpBvnXOOJRLd64LJeHTeCkSuB3lfaR2N0AYBaSi', salt=null WHERE login='admin';
```

### Docker Registry

```bash
# List images in registry
curl http://localhost:5000/v2/_catalog

# List tags for an image
curl http://localhost:5000/v2/bookstore/book-service/tags/list

# Delete image
curl -X DELETE http://localhost:5000/v2/bookstore/book-service/manifests/<digest>
```

## Additional Resources

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Docker Documentation](https://docs.docker.com/)
- [Blue Ocean Documentation](https://www.jenkins.io/doc/book/blueocean/)

## Support

For issues or questions:
1. Check Jenkins console output
2. Review service logs
3. Consult this documentation
4. Contact the DevOps team