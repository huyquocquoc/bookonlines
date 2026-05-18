# Quick Start Guide - Book Management System

## Prerequisites Checklist

Before starting, ensure you have:

- ✅ Java JDK 17+ installed
- ✅ Maven 3.8+ installed
- ✅ Node.js 22.13.1 installed
- ✅ PostgreSQL running on 192.168.1.88:5437
- ✅ Redis running on 192.168.1.88:30073
- ✅ Apache Kafka running (default: localhost:9092)

## Database Setup

### 1. Create the Books Table

Connect to PostgreSQL and run:

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

-- Create indexes for better performance
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category ON books(category);
```

### 2. Insert Sample Data (Optional)

```sql
INSERT INTO books (isbn, title, author, publisher, category, language, description, price, stock_quantity, published_date, page_count, rating, active)
VALUES 
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 'Technology', 'English', 'A comprehensive guide to Java programming best practices', 45.99, 50, '2018-01-06', 416, 4.8, true),
('9780596009205', 'Head First Design Patterns', 'Eric Freeman', 'O''Reilly Media', 'Technology', 'English', 'A brain-friendly guide to design patterns', 39.99, 30, '2004-10-25', 694, 4.5, true),
('9781617294945', 'Spring in Action', 'Craig Walls', 'Manning', 'Technology', 'English', 'Covers Spring 5 and Spring Boot 2', 49.99, 40, '2018-10-03', 520, 4.6, true);
```

## Kafka Setup

### Create Required Topic

```bash
# If using local Kafka
kafka-topics.sh --create --topic book-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify topic creation
kafka-topics.sh --list --bootstrap-server localhost:9092
```

## Backend Startup

### Option 1: Using Maven (Development)

Open 3 separate terminal windows:

**Terminal 1 - Book Service (Port 8080):**
```bash
cd backend/book-service
mvn spring-boot:run
```

**Terminal 2 - Inventory Service (Port 8081):**
```bash
cd backend/inventory-service
mvn spring-boot:run
```

**Terminal 3 - Notification Service (Port 8082):**
```bash
cd backend/notification-service
mvn spring-boot:run
```

### Option 2: Using JAR Files (Production)

First, build all services:
```bash
cd backend
mvn clean install
```

Then run each service:
```bash
# Terminal 1
java -jar backend/book-service/target/book-service-1.0.0.jar

# Terminal 2
java -jar backend/inventory-service/target/inventory-service-1.0.0.jar

# Terminal 3
java -jar backend/notification-service/target/notification-service-1.0.0.jar
```

### Verify Backend is Running

```bash
# Check Book Service
curl http://localhost:8080/api/books

# Check Inventory Service
curl http://localhost:8081/actuator/health

# Check Notification Service
curl http://localhost:8082/actuator/health
```

## Frontend Startup

### Development Mode

```bash
cd frontend
npm install
npm start
```

Access the application at: **http://localhost:4200**

### Production Build

```bash
cd frontend
npm run build

# Serve the production build
npx http-server dist/book-management-app -p 4200
```

## Testing the System

### 1. Create a Book via API

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
    "description": "A test book for demonstration",
    "price": 29.99,
    "stockQuantity": 100,
    "publishedDate": "2024-01-01",
    "pageCount": 300,
    "rating": 4.5,
    "active": true
  }'
```

### 2. Get All Books (Paginated)

```bash
curl http://localhost:8080/api/books?page=0&size=30
```

### 3. Search Books

```bash
curl "http://localhost:8080/api/books/search?query=Java&page=0&size=30"
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

## Using the Frontend

1. **View Books**: Navigate to http://localhost:4200 to see the book list
2. **Search**: Use the search bar to filter books by title, author, or ISBN
3. **Add Book**: Click the "Add Book" button to create a new book
4. **Edit Book**: Click the edit icon on any book row
5. **View Details**: Click on a book title to see full details
6. **Delete Book**: Click the delete icon and confirm

## Monitoring

### Check Redis Cache

```bash
redis-cli -h 192.168.1.88 -p 30073 -a mypassword

# Inside redis-cli:
KEYS books:*
TTL books:page:0:size:30
GET books:page:0:size:30
```

### Monitor Kafka Events

```bash
# Console consumer to see all book events
kafka-console-consumer.sh --topic book-events --from-beginning --bootstrap-server localhost:9092
```

### View Application Logs

Check the console output of each service for logs, or if configured:
```bash
tail -f backend/book-service/logs/application.log
tail -f backend/inventory-service/logs/application.log
tail -f backend/notification-service/logs/application.log
```

## Common Issues and Solutions

### Issue: Jackson Date Serialization Error
**Error:** `InvalidDefinitionException: Java 8 date/time type 'java.time.LocalDate' not supported`

**Solution:** ✅ Already fixed! The system includes:
- Jackson JSR310 dependency in `pom.xml`
- `JacksonConfig.java` that registers `JavaTimeModule`
- Dates are serialized in ISO-8601 format

### Issue: Cannot Connect to PostgreSQL
**Solution:**
- Verify PostgreSQL is running: `psql -h 192.168.1.88 -p 5437 -U postgres -d postgres`
- Check credentials in `application.yml`
- Ensure firewall allows connection to port 5437

### Issue: Cannot Connect to Redis
**Solution:**
- Test connection: `redis-cli -h 192.168.1.88 -p 30073 -a mypassword ping`
- Should return: `PONG`
- Check Redis is running and accessible

### Issue: Kafka Connection Failed
**Solution:**
- Ensure Kafka is running: `jps | grep Kafka`
- Check Zookeeper is running: `jps | grep QuorumPeerMain`
- Verify bootstrap servers in `application.yml`

### Issue: Frontend Cannot Connect to Backend
**Solution:**
- Verify Book Service is running on port 8080
- Check browser console for CORS errors
- Ensure `proxy.conf.json` is configured correctly

### Issue: Port Already in Use
**Solution:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

## Architecture Overview

```
┌─────────────────┐
│   Angular 18    │
│   Frontend      │
│   Port: 4200    │
└────────┬────────┘
         │ HTTP REST
         ▼
┌─────────────────┐      ┌──────────────┐
│  Book Service   │─────▶│  PostgreSQL  │
│   Port: 8080    │      │  Port: 5437  │
└────────┬────────┘      └──────────────┘
         │
         ├─────────────▶ Redis Cache (Port: 30073)
         │
         │ Kafka Events
         ▼
    ┌────────────────────────┐
    │   Kafka Topic:         │
    │   book-events          │
    └───┬────────────────┬───┘
        │                │
        ▼                ▼
┌───────────────┐  ┌──────────────────┐
│  Inventory    │  │  Notification    │
│  Service      │  │  Service         │
│  Port: 8081   │  │  Port: 8082      │
└───────────────┘  └──────────────────┘
```

## Performance Features

- **Redis Caching**: 1-hour TTL for all GET requests
- **Pagination**: Default 30 rows per page
- **Virtual Scrolling**: Efficient rendering of large datasets in frontend
- **Async Communication**: Kafka for non-blocking service communication
- **Database Indexing**: Optimized queries on frequently searched columns

## Next Steps

1. ✅ System is ready to use
2. Add authentication/authorization (JWT)
3. Implement comprehensive testing
4. Set up CI/CD pipeline
5. Add monitoring (Prometheus/Grafana)
6. Deploy to production environment

## Support

For detailed information, refer to:
- `ARCHITECTURE.md` - System architecture and design
- `IMPLEMENTATION_GUIDE.md` - Technical implementation details
- `DEPLOYMENT_GUIDE.md` - Complete deployment instructions
- `README.md` - Project overview

## Connection Details Summary

| Service | Host | Port | Credentials |
|---------|------|------|-------------|
| PostgreSQL | 192.168.1.88 | 5437 | postgres/All4one7! |
| Redis | 192.168.1.88 | 30073 | default/mypassword |
| Kafka | localhost | 9092 | - |
| Book Service | localhost | 8080 | - |
| Inventory Service | localhost | 8081 | - |
| Notification Service | localhost | 8082 | - |
| Frontend | localhost | 4200 | - |