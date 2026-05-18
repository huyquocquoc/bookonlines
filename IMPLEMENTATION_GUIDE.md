# Implementation Guide - Book Management System

This guide provides step-by-step instructions for implementing the event-driven microservices system.

## Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: Version 22.13.1
- **Maven**: 3.8+
- **PostgreSQL**: Running at 192.168.1.88:5437
- **Redis**: Running at 192.168.1.88:30073
- **Kafka**: Local or remote instance
- **IDE**: IntelliJ IDEA or VS Code

## Phase 1: Backend Setup

### Step 1: Create Multi-Module Maven Project

```
bookonline5/
├── backend/
│   ├── pom.xml (parent)
│   ├── book-service/
│   │   ├── pom.xml
│   │   └── src/
│   ├── inventory-service/
│   │   ├── pom.xml
│   │   └── src/
│   ├── notification-service/
│   │   ├── pom.xml
│   │   └── src/
│   └── common/
│       ├── pom.xml
│       └── src/
```

### Step 2: Parent POM Dependencies

Key dependencies to include:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-data-redis
- spring-kafka
- postgresql driver
- lombok
- spring-boot-starter-validation
- spring-boot-starter-actuator

### Step 3: Book Service Implementation

**Core Components**:
1. **Entity**: [`Book.java`](backend/book-service/src/main/java/com/bookstore/entity/Book.java)
2. **Repository**: [`BookRepository.java`](backend/book-service/src/main/java/com/bookstore/repository/BookRepository.java)
3. **Service**: [`BookService.java`](backend/book-service/src/main/java/com/bookstore/service/BookService.java)
4. **Controller**: [`BookController.java`](backend/book-service/src/main/java/com/bookstore/controller/BookController.java)
5. **Kafka Producer**: [`BookEventProducer.java`](backend/book-service/src/main/java/com/bookstore/kafka/BookEventProducer.java)

**Configuration Files**:
- [`application.yml`](backend/book-service/src/main/resources/application.yml) - Database, Redis, Kafka config
- [`RedisConfig.java`](backend/book-service/src/main/java/com/bookstore/config/RedisConfig.java) - Redis cache setup
- [`KafkaProducerConfig.java`](backend/book-service/src/main/java/com/bookstore/config/KafkaProducerConfig.java)

### Step 4: Inventory Service Implementation

**Core Components**:
1. **Kafka Consumer**: [`BookEventConsumer.java`](backend/inventory-service/src/main/java/com/bookstore/inventory/kafka/BookEventConsumer.java)
2. **Service**: [`InventoryService.java`](backend/inventory-service/src/main/java/com/bookstore/inventory/service/InventoryService.java)
3. **Entity**: [`Inventory.java`](backend/inventory-service/src/main/java/com/bookstore/inventory/entity/Inventory.java)

**Event Handling**:
- BOOK_CREATED → Initialize stock
- BOOK_UPDATED → Update stock quantity
- BOOK_DELETED → Remove inventory record

### Step 5: Notification Service Implementation

**Core Components**:
1. **Kafka Consumer**: [`BookEventConsumer.java`](backend/notification-service/src/main/java/com/bookstore/notification/kafka/BookEventConsumer.java)
2. **Service**: [`NotificationService.java`](backend/notification-service/src/main/java/com/bookstore/notification/service/NotificationService.java)
3. **Email Service**: [`EmailService.java`](backend/notification-service/src/main/java/com/bookstore/notification/service/EmailService.java)

**Notification Types**:
- New book added
- Book updated
- Book deleted
- Low stock alert

## Phase 2: Frontend Setup

### Step 1: Create Angular 18 Project

```bash
cd frontend
npx @angular/cli@18 new book-management-app
# Select: Yes for routing, CSS for styling
```

### Step 2: Install Dependencies

```bash
npm install @angular/material @angular/cdk
npm install @angular/common/http
npm install rxjs
```

### Step 3: Project Structure

```
frontend/
└── src/
    └── app/
        ├── core/
        │   ├── models/
        │   │   ├── book.model.ts
        │   │   ├── page-response.model.ts
        │   │   └── api-response.model.ts
        │   ├── services/
        │   │   ├── book.service.ts
        │   │   └── notification.service.ts
        │   └── interceptors/
        │       └── http-error.interceptor.ts
        ├── features/
        │   └── books/
        │       ├── book-list/
        │       ├── book-detail/
        │       ├── book-form/
        │       └── book-delete-dialog/
        ├── shared/
        │   ├── components/
        │   └── pipes/
        └── app.routes.ts
```

### Step 4: Key Angular Components

**Book Service** ([`book.service.ts`](frontend/src/app/core/services/book.service.ts)):
- HTTP methods for CRUD operations
- Observable-based async calls
- Error handling

**Book List Component** ([`book-list.component.ts`](frontend/src/app/features/books/book-list/book-list.component.ts)):
- Virtual scrolling with CDK
- Pagination (30 rows per page)
- Search and filter
- Loading states

**Book Form Component** ([`book-form.component.ts`](frontend/src/app/features/books/book-form/book-form.component.ts)):
- Reactive forms
- Validation
- Create/Edit modes

## Phase 3: Integration

### Step 1: CORS Configuration

In Book Service, add CORS configuration:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

### Step 2: Angular Environment Configuration

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Step 3: Proxy Configuration (Optional)

Create [`proxy.conf.json`](frontend/proxy.conf.json):

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

## Phase 4: Testing

### Backend Testing

1. **Unit Tests**: Test services and repositories
2. **Integration Tests**: Test REST endpoints
3. **Kafka Tests**: Test event publishing/consuming

### Frontend Testing

1. **Unit Tests**: Test components and services
2. **E2E Tests**: Test user workflows
3. **Integration Tests**: Test API communication

## Phase 5: Running the Application

### Start Backend Services

```bash
# Terminal 1 - Book Service
cd backend/book-service
mvn spring-boot:run

# Terminal 2 - Inventory Service
cd backend/inventory-service
mvn spring-boot:run

# Terminal 3 - Notification Service
cd backend/notification-service
mvn spring-boot:run
```

### Start Frontend

```bash
cd frontend
npm start
# Access at http://localhost:4200
```

## API Endpoints Reference

### Books API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/books` | List all books (paginated) |
| GET | `/api/books/{id}` | Get book by ID |
| POST | `/api/books` | Create new book |
| PUT | `/api/books/{id}` | Update book |
| DELETE | `/api/books/{id}` | Delete book |
| GET | `/api/books/search` | Search books |

### Query Parameters

- `page`: Page number (default: 0)
- `size`: Page size (default: 30)
- `sort`: Sort field and direction (e.g., `title,asc`)
- `search`: Search query
- `category`: Filter by category
- `author`: Filter by author

## Configuration Files Summary

### Backend Configuration

**application.yml** (Book Service):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://192.168.1.88:5437/postgres
    username: postgres
    password: All4one7!
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: 192.168.1.88
      port: 30073
      username: default
      password: mypassword
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8080

cache:
  ttl: 3600 # 1 hour in seconds
```

### Frontend Configuration

**angular.json** - Add proxy configuration:
```json
{
  "serve": {
    "options": {
      "proxyConfig": "proxy.conf.json"
    }
  }
}
```

## Performance Optimization

### Backend
1. **Database Indexing**: Create indexes on frequently queried columns
2. **Connection Pooling**: Configure HikariCP
3. **Redis Caching**: Cache frequently accessed data
4. **Async Processing**: Use Kafka for non-blocking operations

### Frontend
1. **Virtual Scrolling**: Render only visible items
2. **Lazy Loading**: Load modules on demand
3. **Change Detection**: Use OnPush strategy
4. **HTTP Caching**: Cache GET requests

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running at 192.168.1.88:5437
   - Check credentials and database name

2. **Redis Connection Failed**
   - Verify Redis is running at 192.168.1.88:30073
   - Check username and password

3. **Kafka Connection Failed**
   - Ensure Kafka broker is running
   - Verify bootstrap servers configuration

4. **CORS Errors**
   - Check CORS configuration in backend
   - Verify allowed origins include frontend URL

5. **Angular Build Errors**
   - Ensure Node.js version 22.13.1 is installed
   - Clear node_modules and reinstall

## Next Steps After Implementation

1. **Security**: Add JWT authentication
2. **Monitoring**: Integrate Prometheus and Grafana
3. **Logging**: Set up ELK stack
4. **CI/CD**: Configure Jenkins or GitHub Actions
5. **Containerization**: Create Docker images
6. **Orchestration**: Deploy to Kubernetes