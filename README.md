# Book Management System - Event-Driven Microservices

A modern, scalable book management system built with event-driven microservices architecture using Spring Boot and Angular 18.
BOB and Codex
## 🚀 Features

### Backend
- ✅ **RESTful API** with Spring Boot 3.x
- ✅ **Event-Driven Architecture** using Apache Kafka
- ✅ **Redis Caching** with 1-hour TTL for optimized performance
- ✅ **PostgreSQL Database** with JPA/Hibernate
- ✅ **Microservices**: Book Service, Inventory Service, Notification Service
- ✅ **Pagination Support** (30 rows per page)
- ✅ **Async Communication** between services via Kafka events

### Frontend
- ✅ **Angular 18** (compatible with Node.js 22.13.1)
- ✅ **Virtual Scrolling** for large datasets
- ✅ **Reactive Forms** with validation
- ✅ **Material Design** UI components
- ✅ **Async HTTP Calls** with RxJS
- ✅ **Search & Filter** functionality
- ✅ **Responsive Design**

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: Version 22.13.1
- **Maven**: 3.8+
- **PostgreSQL**: 13+
- **Redis**: 6+
- **Apache Kafka**: 3.0+

## 🏗️ Architecture

```
┌─────────────────┐
│  Angular 18 App │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────┐      ┌──────────────┐
│  Book Service   │─────▶│  PostgreSQL  │
│   (Port 8080)   │      └──────────────┘
└────────┬────────┘
         │                ┌──────────────┐
         ├───────────────▶│    Redis     │
         │                └──────────────┘
         │ Kafka Events
         ▼
┌─────────────────────────────────────┐
│         Apache Kafka Broker         │
└──────────┬──────────────────┬───────┘
           │                  │
           ▼                  ▼
┌──────────────────┐  ┌──────────────────┐
│ Inventory Service│  │Notification Svc  │
│   (Port 8081)    │  │   (Port 8082)    │
└──────────────────┘  └──────────────────┘
```

## 📁 Project Structure

```
bookonline5/
├── backend/
│   ├── book-service/          # Main REST API service
│   ├── inventory-service/     # Stock management service
│   ├── notification-service/  # Alert and notification service
│   └── common/                # Shared models and utilities
├── frontend/                  # Angular 18 application
├── ARCHITECTURE.md           # Detailed architecture documentation
├── IMPLEMENTATION_GUIDE.md   # Step-by-step implementation guide
└── README.md                 # This file
```

## 🔧 Configuration

### Database Configuration
```yaml
PostgreSQL:
  Host: 192.168.1.88
  Port: 5437
  Database: postgres
  Username: postgres
  Password: All4one7!
  Table: books
```

### Redis Configuration
```yaml
Redis:
  Host: 192.168.1.88
  Port: 30073
  Username: default
  Password: mypassword
  TTL: 3600 seconds (1 hour)
```

### Kafka Configuration
```yaml
Kafka:
  Bootstrap Servers: localhost:9092
  Topic: book-events
  Consumer Groups:
    - inventory-service-group
    - notification-service-group
```

## 🗄️ Database Schema

```sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    category VARCHAR(100),
    language VARCHAR(50) DEFAULT 'English',
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    stock_quantity INTEGER DEFAULT 0 NOT NULL,
    published_date DATE,
    page_count INTEGER,
    cover_image_url TEXT,
    rating NUMERIC(2, 1) DEFAULT 0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd bookonline5
```

### 2. Start Backend Services

#### Book Service
```bash
cd backend/book-service
mvn clean install
mvn spring-boot:run
```

#### Inventory Service
```bash
cd backend/inventory-service
mvn clean install
mvn spring-boot:run
```

#### Notification Service
```bash
cd backend/notification-service
mvn clean install
mvn spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend
npm install
npm start
```

Access the application at: `http://localhost:4200`

## 📡 API Endpoints

### Books API (`/api/books`)

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/api/books` | List all books (paginated) | - |
| GET | `/api/books/{id}` | Get book by ID | - |
| POST | `/api/books` | Create new book | Book JSON |
| PUT | `/api/books/{id}` | Update book | Book JSON |
| DELETE | `/api/books/{id}` | Delete book | - |
| GET | `/api/books/search` | Search books | Query params |

### Query Parameters
- `page`: Page number (default: 0)
- `size`: Page size (default: 30)
- `sort`: Sort field and direction (e.g., `title,asc`)
- `search`: Search query
- `category`: Filter by category
- `author`: Filter by author

### Example Request
```bash
# Get paginated books
curl http://localhost:8080/api/books?page=0&size=30

# Search books
curl http://localhost:8080/api/books/search?search=Java&category=Programming

# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "978-0-123456-78-9",
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "price": 49.99,
    "stockQuantity": 100
  }'
```

## 🎯 Key Features Implementation

### 1. Pagination
- Default page size: 30 rows
- Supports custom page sizes
- Returns total pages and elements

### 2. Redis Caching
- Caches individual book lookups
- Caches paginated results
- 1-hour TTL for all cached data
- Automatic cache invalidation on updates

### 3. Kafka Events
Event types published:
- `BOOK_CREATED`: When a new book is added
- `BOOK_UPDATED`: When a book is modified
- `BOOK_DELETED`: When a book is removed

### 4. Virtual Scrolling (Frontend)
- Efficient rendering of large lists
- Loads data in chunks
- Smooth scrolling experience
- Optimized performance

## 🧪 Testing

### Backend Tests
```bash
cd backend/book-service
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
npm run e2e
```

## 📊 Monitoring & Observability

The system includes comprehensive monitoring with **Prometheus** and **Grafana**.

### Quick Start Monitoring

```bash
# Start Prometheus and Grafana
docker-compose -f docker-compose-monitoring.yml up -d

# Access monitoring tools
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/admin)
```

### Available Dashboards

- **Spring Boot Microservices Dashboard**: Pre-configured dashboard with:
  - HTTP request rates and response times
  - JVM memory and CPU usage
  - Database connection pool metrics
  - Thread counts and GC metrics

### Health Checks
- Book Service: `http://localhost:8081/actuator/health`
- Auth Service: `http://localhost:8086/actuator/health`
- Cart Service: `http://localhost:8084/actuator/health`
- Inventory Service: `http://localhost:8082/actuator/health`
- Notification Service: `http://localhost:8083/actuator/health`

### Prometheus Metrics
All services expose Prometheus metrics at `/actuator/prometheus`:
- `http://localhost:8081/actuator/prometheus` (Book Service)
- `http://localhost:8086/actuator/prometheus` (Auth Service)
- `http://localhost:8084/actuator/prometheus` (Cart Service)
- `http://localhost:8082/actuator/prometheus` (Inventory Service)
- `http://localhost:8083/actuator/prometheus` (Notification Service)

### Key Metrics Monitored
- HTTP request rate and latency
- JVM memory usage (heap/non-heap)
- CPU utilization (system/process)
- Database connection pool status
- Thread counts and states
- Garbage collection metrics

For detailed monitoring setup and configuration, see [MONITORING_GUIDE.md](MONITORING_GUIDE.md).

## 🔒 Security Considerations

- Input validation on all endpoints
- SQL injection prevention via JPA
- CORS configuration for frontend
- Ready for JWT/OAuth2 integration

## 📈 Performance Optimization

1. **Database Indexing**: Indexes on isbn, title, author, category
2. **Connection Pooling**: HikariCP configuration
3. **Redis Caching**: Reduces database load
4. **Async Processing**: Kafka for non-blocking operations
5. **Virtual Scrolling**: Efficient frontend rendering

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify PostgreSQL is running at 192.168.1.88:5437
- Check credentials in application.yml

**Redis Connection Failed**
- Verify Redis is running at 192.168.1.88:30073
- Check username and password

**Kafka Connection Failed**
- Ensure Kafka broker is running
- Verify bootstrap servers configuration

**CORS Errors**
- Check CORS configuration in WebConfig
- Verify allowed origins include frontend URL

## 📚 Documentation

- [Architecture Documentation](ARCHITECTURE.md) - Detailed system architecture
- [Implementation Guide](IMPLEMENTATION_GUIDE.md) - Step-by-step implementation instructions
- [Monitoring Guide](MONITORING_GUIDE.md) - Prometheus and Grafana setup and usage

## 🛠️ Technology Stack

### Backend
- Spring Boot 3.x
- Spring Data JPA
- Spring Kafka
- Spring Cache
- PostgreSQL
- Redis
- Lombok
- Maven

### Frontend
- Angular 18
- TypeScript
- RxJS
- Angular Material
- CDK Virtual Scroll

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- Angular team for powerful frontend framework
- Apache Kafka for reliable event streaming