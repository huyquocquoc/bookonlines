# Deployment Guide - Book Management System

## Overview
This guide provides step-by-step instructions to deploy and run the complete Book Management System with event-driven microservices architecture.

## Prerequisites

### Required Software
- **Java**: JDK 17 or higher
- **Maven**: 3.8 or higher
- **Node.js**: 22.13.1 (as specified)
- **PostgreSQL**: 12 or higher (running on 192.168.1.88:5437)
- **Redis**: 6 or higher (running on 192.168.1.88:30073)
- **Apache Kafka**: 3.x or higher

### Database Setup
The system connects to:
- **PostgreSQL**
  - Host: 192.168.1.88
  - Port: 5437
  - Database: postgres
  - Username: postgres
  - Password: All4one7!
  - Table: books

- **Redis**
  - Host: 192.168.1.88
  - Port: 30073
  - Username: default
  - Password: mypassword

## Backend Deployment

### 1. Build All Microservices

Navigate to the backend directory and build the project:

```bash
cd backend
mvn clean install
```

This will:
- Build the common module with shared DTOs
- Build all three microservices (book-service, inventory-service, notification-service)
- Run tests
- Create executable JAR files

### 2. Start Kafka (if not already running)

Ensure Kafka is running on your system. If using local Kafka:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 3. Create Kafka Topics

Create the required Kafka topic:

```bash
kafka-topics.sh --create --topic book-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### 4. Start Microservices

Start each microservice in separate terminal windows:

#### Book Service (Port 8080)
```bash
cd backend/book-service
mvn spring-boot:run
```

Or using the JAR:
```bash
java -jar target/book-service-1.0.0.jar
```

#### Inventory Service (Port 8081)
```bash
cd backend/inventory-service
mvn spring-boot:run
```

Or using the JAR:
```bash
java -jar target/inventory-service-1.0.0.jar
```

#### Notification Service (Port 8082)
```bash
cd backend/notification-service
mvn spring-boot:run
```

Or using the JAR:
```bash
java -jar target/notification-service-1.0.0.jar
```

### 5. Verify Backend Services

Check that all services are running:

```bash
# Book Service
curl http://localhost:8080/api/books

# Inventory Service Health
curl http://localhost:8081/actuator/health

# Notification Service Health
curl http://localhost:8082/actuator/health
```

## Frontend Deployment

### 1. Install Dependencies

Navigate to the frontend directory:

```bash
cd frontend
npm install
```

### 2. Development Mode

Run the Angular application in development mode:

```bash
npm start
```

The application will be available at: http://localhost:4200

### 3. Production Build

Build the application for production:

```bash
npm run build
```

The production files will be in `frontend/dist/book-management-app/`

### 4. Serve Production Build

You can serve the production build using a web server like nginx or a simple HTTP server:

```bash
# Using Node.js http-server
npx http-server dist/book-management-app -p 4200

# Or using Python
cd dist/book-management-app
python -m http.server 4200
```

## Database Schema

The `books` table should have the following structure:

```sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    category VARCHAR(100),
    language VARCHAR(50),
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    published_date DATE,
    page_count INTEGER,
    cover_image_url VARCHAR(500),
    rating DECIMAL(3, 2),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category ON books(category);
```

## API Endpoints

### Book Service (Port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/books | Get all books (paginated) |
| GET | /api/books/{id} | Get book by ID |
| GET | /api/books/search?query={query} | Search books |
| GET | /api/books/category/{category} | Get books by category |
| GET | /api/books/author/{author} | Get books by author |
| POST | /api/books | Create new book |
| PUT | /api/books/{id} | Update book |
| DELETE | /api/books/{id} | Delete book |

### Query Parameters for Pagination
- `page`: Page number (default: 0)
- `size`: Page size (default: 30)

## Testing the System

### 1. Create a Book

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9781234567890",
    "title": "Test Book",
    "author": "John Doe",
    "publisher": "Test Publisher",
    "category": "Fiction",
    "language": "English",
    "description": "A test book",
    "price": 29.99,
    "stockQuantity": 100,
    "publishedDate": "2024-01-01",
    "pageCount": 300,
    "rating": 4.5,
    "active": true
  }'
```

### 2. Get All Books

```bash
curl http://localhost:8080/api/books?page=0&size=30
```

### 3. Search Books

```bash
curl http://localhost:8080/api/books/search?query=Test&page=0&size=30
```

### 4. Update a Book

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 24.99,
    "stockQuantity": 150
  }'
```

### 5. Delete a Book

```bash
curl -X DELETE http://localhost:8080/api/books/1
```

## Event Flow

When a book operation occurs:

1. **Book Service** processes the request and publishes an event to Kafka topic `book-events`
2. **Inventory Service** consumes the event and updates inventory records
3. **Notification Service** consumes the event and logs/sends notifications

Event types:
- `BOOK_CREATED`: When a new book is added
- `BOOK_UPDATED`: When a book is modified
- `BOOK_DELETED`: When a book is removed

## Monitoring

### Redis Cache Monitoring

Check Redis cache:

```bash
redis-cli -h 192.168.1.88 -p 30073 -a mypassword
> KEYS books:*
> TTL books:page:0:size:30
```

### Kafka Monitoring

Check Kafka topics and messages:

```bash
# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Consume messages
kafka-console-consumer.sh --topic book-events --from-beginning --bootstrap-server localhost:9092
```

### Application Logs

Check logs for each service:

```bash
# Book Service
tail -f backend/book-service/logs/application.log

# Inventory Service
tail -f backend/inventory-service/logs/application.log

# Notification Service
tail -f backend/notification-service/logs/application.log
```

## Troubleshooting

### Common Issues

1. **Cannot connect to PostgreSQL**
   - Verify PostgreSQL is running on 192.168.1.88:5437
   - Check credentials: postgres/All4one7!
   - Ensure database 'postgres' exists

2. **Cannot connect to Redis**
   - Verify Redis is running on 192.168.1.88:30073
   - Check credentials: default/mypassword
   - Test connection: `redis-cli -h 192.168.1.88 -p 30073 -a mypassword ping`

3. **Kafka connection issues**
   - Ensure Kafka is running
   - Check bootstrap servers configuration
   - Verify topic 'book-events' exists

4. **Frontend cannot connect to backend**
   - Verify Book Service is running on port 8080
   - Check CORS configuration in WebConfig.java
   - Ensure proxy configuration in frontend is correct

5. **Build errors in Angular**
   - Run `npm install` to ensure all dependencies are installed
   - Clear node_modules and reinstall: `rm -rf node_modules && npm install`
   - Check Node.js version: `node --version` (should be 22.13.1)

## Performance Optimization

### Redis Cache
- Cache TTL: 1 hour (3600 seconds)
- Cached queries: All GET requests for books
- Cache eviction: Automatic on UPDATE/DELETE operations

### Pagination
- Default page size: 30 rows
- Maximum recommended: 100 rows per page
- Virtual scrolling in frontend for large datasets

### Database Indexes
- Ensure indexes are created on frequently queried columns (isbn, title, author, category)

## Security Considerations

1. **Database Credentials**: Store in environment variables or secure vault
2. **Redis Password**: Use strong password and restrict access
3. **API Security**: Consider adding Spring Security with JWT authentication
4. **CORS**: Configure allowed origins appropriately for production
5. **Input Validation**: All inputs are validated on both frontend and backend

## Scaling Recommendations

1. **Horizontal Scaling**: Run multiple instances of each microservice behind a load balancer
2. **Database**: Use PostgreSQL replication for read replicas
3. **Redis**: Use Redis Cluster for high availability
4. **Kafka**: Increase partitions for higher throughput
5. **Frontend**: Use CDN for static assets

## Next Steps

1. Add authentication and authorization
2. Implement comprehensive unit and integration tests
3. Set up CI/CD pipeline
4. Add monitoring and alerting (Prometheus, Grafana)
5. Implement API rate limiting
6. Add API documentation (Swagger/OpenAPI)
7. Set up Docker containers for easier deployment
8. Implement backup and disaster recovery procedures