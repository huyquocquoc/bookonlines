# API Documentation Guide - Swagger/OpenAPI

This guide explains how to use and access the API documentation for the Bookstore microservices application using Swagger/OpenAPI.

## Table of Contents

1. [Overview](#overview)
2. [Setup](#setup)
3. [Accessing Swagger UI](#accessing-swagger-ui)
4. [API Endpoints](#api-endpoints)
5. [Authentication](#authentication)
6. [Using the Interactive Documentation](#using-the-interactive-documentation)
7. [OpenAPI Specification](#openapi-specification)
8. [Best Practices](#best-practices)

## Overview

The Bookstore application uses **SpringDoc OpenAPI 3** (formerly Springfox) to automatically generate interactive API documentation. This provides:

- **Interactive API Explorer**: Test APIs directly from the browser
- **Automatic Documentation**: Generated from code annotations
- **Schema Definitions**: Complete request/response models
- **Authentication Support**: JWT token integration
- **Export Capabilities**: Download OpenAPI spec in JSON/YAML

### Technology Stack

- **SpringDoc OpenAPI**: v2.3.0
- **OpenAPI Specification**: v3.0
- **Swagger UI**: Embedded web interface

## Setup

### 1. Add Dependencies

The SpringDoc OpenAPI dependency is already added to `book-service/pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2. Configuration

The OpenAPI configuration is in `OpenApiConfig.java`:

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bookServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Service API")
                        .description("REST API for managing books")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

### 3. Application Properties

Add to `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
  show-actuator: false
```

## Accessing Swagger UI

### Book Service

Once the book-service is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON Specification

Download the raw OpenAPI specification:

```
http://localhost:8080/api-docs
```

### OpenAPI YAML Specification

```
http://localhost:8080/api-docs.yaml
```

## API Endpoints

### Book Service Endpoints

#### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/books` | Get all books (paginated) |
| GET | `/api/books/{id}` | Get book by ID |
| GET | `/api/books/search` | Search books |
| GET | `/api/books/category/{category}` | Get books by category |
| GET | `/api/books/author/{author}` | Get books by author |
| GET | `/api/books/count` | Get total count of active books |

#### Protected Endpoints (Authentication Required)

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/books` | Create new book | Authenticated |
| PUT | `/api/books/{id}` | Update book | ADMIN, DEV |
| DELETE | `/api/books/{id}` | Soft delete book | ADMIN, DEV |
| DELETE | `/api/books/{id}/hard` | Permanently delete book | ADMIN, DEV |

### Auth Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Cart Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cart` | Get user's cart |
| POST | `/api/cart/items` | Add item to cart |
| PUT | `/api/cart/items/{itemId}` | Update cart item |
| DELETE | `/api/cart/items/{itemId}` | Remove item from cart |
| POST | `/api/cart/checkout` | Create checkout session |

## Authentication

### Getting a JWT Token

1. **Sign Up** (if you don't have an account):
   ```bash
   curl -X POST http://localhost:8081/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "test@example.com",
       "password": "password123"
     }'
   ```

2. **Login** to get JWT token:
   ```bash
   curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "password123"
     }'
   ```

   Response:
   ```json
   {
     "success": true,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "type": "Bearer",
       "username": "testuser",
       "email": "test@example.com"
     }
   }
   ```

### Using JWT Token in Swagger UI

1. Click the **"Authorize"** button at the top right
2. Enter your JWT token in the format: `Bearer <your-token>`
3. Click **"Authorize"**
4. Click **"Close"**

Now all authenticated requests will include the JWT token automatically.

### Using JWT Token with cURL

```bash
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Using the Interactive Documentation

### 1. Explore Endpoints

- Browse available endpoints organized by tags
- Click on any endpoint to expand details
- View request parameters, body schemas, and response formats

### 2. Try It Out

1. Click **"Try it out"** button on any endpoint
2. Fill in required parameters
3. For authenticated endpoints, authorize first
4. Click **"Execute"**
5. View the response below

### 3. Example: Creating a Book

1. Navigate to **POST /api/books**
2. Click **"Try it out"**
3. Ensure you're authenticated (click Authorize if not)
4. Enter book details:
   ```json
   {
     "title": "Clean Code",
     "author": "Robert C. Martin",
     "isbn": "978-0132350884",
     "price": 44.99,
     "category": "Programming",
     "description": "A Handbook of Agile Software Craftsmanship",
     "stockQuantity": 50,
     "imageUrl": "https://example.com/clean-code.jpg"
   }
   ```
5. Click **"Execute"**
6. View the response

### 4. Example: Searching Books

1. Navigate to **GET /api/books/search**
2. Click **"Try it out"**
3. Enter search query: `Java`
4. Set page: `0`
5. Set size: `10`
6. Click **"Execute"**
7. View paginated results

## OpenAPI Specification

### Downloading the Specification

**JSON Format:**
```bash
curl http://localhost:8080/api-docs > openapi.json
```

**YAML Format:**
```bash
curl http://localhost:8080/api-docs.yaml > openapi.yaml
```

### Using with API Clients

#### Postman

1. Open Postman
2. Click **Import**
3. Select **Link** tab
4. Enter: `http://localhost:8080/api-docs`
5. Click **Continue** → **Import**

#### Insomnia

1. Open Insomnia
2. Click **Create** → **Import From** → **URL**
3. Enter: `http://localhost:8080/api-docs`
4. Click **Fetch and Import**

#### VS Code REST Client

Create a `.http` file:

```http
### Get all books
GET http://localhost:8080/api/books?page=0&size=10

### Get book by ID
GET http://localhost:8080/api/books/1

### Create book (requires auth)
POST http://localhost:8080/api/books
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "isbn": "978-0134685991",
  "price": 54.99,
  "category": "Programming",
  "description": "Best practices for Java programming",
  "stockQuantity": 30
}
```

## Best Practices

### 1. API Documentation Annotations

Always use comprehensive OpenAPI annotations:

```java
@Operation(
    summary = "Create a new book",
    description = "Add a new book to the inventory. Requires authentication.",
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "Book created successfully",
        content = @Content(schema = @Schema(implementation = ApiResponse.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid book data",
        content = @Content
    )
})
@PostMapping
public ResponseEntity<ApiResponse<BookDTO>> createBook(
    @Parameter(description = "Book details", required = true)
    @Valid @RequestBody BookDTO bookDTO) {
    // Implementation
}
```

### 2. Schema Documentation

Document DTOs with schema annotations:

```java
@Schema(description = "Book data transfer object")
public class BookDTO {
    
    @Schema(description = "Unique identifier", example = "1", accessMode = READ_ONLY)
    private Long id;
    
    @Schema(description = "Book title", example = "Clean Code", required = true)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Schema(description = "Book price in USD", example = "44.99", required = true)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal price;
}
```

### 3. Response Examples

Provide example responses:

```java
@ApiResponse(
    responseCode = "200",
    description = "Success",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ApiResponse.class),
        examples = @ExampleObject(
            value = "{\"success\":true,\"data\":{\"id\":1,\"title\":\"Clean Code\"}}"
        )
    )
)
```

### 4. Error Documentation

Document all possible error responses:

```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
```

### 5. Grouping Endpoints

Use tags to organize endpoints:

```java
@Tag(name = "Books", description = "Book management APIs")
@RestController
@RequestMapping("/api/books")
public class BookController {
    // Endpoints
}
```

### 6. Hide Internal Endpoints

Hide actuator or internal endpoints:

```yaml
springdoc:
  show-actuator: false
  paths-to-exclude: /internal/**
```

## Common Issues and Solutions

### Issue: Swagger UI Not Loading

**Solution:**
1. Check if service is running: `curl http://localhost:8080/actuator/health`
2. Verify SpringDoc dependency is in pom.xml
3. Check application logs for errors
4. Try accessing: `http://localhost:8080/v3/api-docs`

### Issue: Authentication Not Working

**Solution:**
1. Ensure JWT token is valid and not expired
2. Use correct format: `Bearer <token>`
3. Check SecurityConfig allows Swagger endpoints:
   ```java
   .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
   ```

### Issue: Endpoints Not Showing

**Solution:**
1. Verify controller has `@RestController` annotation
2. Check `@RequestMapping` path
3. Ensure methods are public
4. Add `@Tag` annotation to controller

### Issue: Schema Not Displaying Correctly

**Solution:**
1. Add `@Schema` annotations to DTOs
2. Use proper Jackson annotations
3. Ensure DTOs are public classes
4. Check for circular references

## Advanced Configuration

### Custom Swagger UI Theme

```yaml
springdoc:
  swagger-ui:
    syntaxHighlight:
      theme: monokai
    displayRequestDuration: true
    filter: true
```

### Multiple API Groups

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/books/**")
            .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/admin/**")
            .build();
}
```

### Custom Operation Customizer

```java
@Bean
public OperationCustomizer customize() {
    return (operation, handlerMethod) -> {
        operation.addParametersItem(
            new Parameter()
                .in("header")
                .name("X-Request-ID")
                .description("Request correlation ID")
                .required(false)
        );
        return operation;
    };
}
```

## Testing APIs

### Using cURL

```bash
# Get all books
curl -X GET "http://localhost:8080/api/books?page=0&size=10"

# Get book by ID
curl -X GET "http://localhost:8080/api/books/1"

# Create book (with auth)
curl -X POST "http://localhost:8080/api/books" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "price": 44.99,
    "category": "Programming",
    "stockQuantity": 50
  }'

# Update book
curl -X PUT "http://localhost:8080/api/books/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code - Updated",
    "price": 39.99
  }'

# Delete book
curl -X DELETE "http://localhost:8080/api/books/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Using HTTPie

```bash
# Get all books
http GET http://localhost:8080/api/books page==0 size==10

# Create book
http POST http://localhost:8080/api/books \
  Authorization:"Bearer YOUR_TOKEN" \
  title="Clean Code" \
  author="Robert C. Martin" \
  isbn="978-0132350884" \
  price:=44.99 \
  category="Programming" \
  stockQuantity:=50
```

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [JWT.io](https://jwt.io/) - JWT token debugger

## Support

For issues or questions:
1. Check this documentation
2. Review service logs
3. Test endpoints with cURL
4. Verify authentication tokens
5. Contact the development team