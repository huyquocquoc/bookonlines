# JWT Role-Based Security Implementation Guide

## Overview
This guide documents the JWT role-based authorization implementation for the book-service API endpoints. The UPDATE and DELETE operations on `/api/books` are now protected and require `ADMIN_ROLE` or `DEV_ROLE` permissions.

## Database Schema Changes
The authentication system now uses a many-to-many relationship for user roles:
- **users** table: Stores user information (id, email, password, first_name, last_name, active, created_at, updated_at)
- **user_roles** table: Junction table storing user-role associations (user_id, role)
- Users can have multiple roles (stored as a Set<UserRole>)

## Implementation Components

### 1. Dependencies Added (pom.xml)
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. Security Components Created

#### JwtUtil.java
- Location: `src/main/java/com/bookstore/security/JwtUtil.java`
- Purpose: Validates JWT tokens and extracts user information and roles
- Key Methods:
  - `extractUsername(String token)`: Extracts username from JWT
  - `extractRoles(String token)`: Extracts user roles from JWT
  - `validateToken(String token)`: Validates token expiration and signature

#### JwtAuthenticationFilter.java
- Location: `src/main/java/com/bookstore/security/JwtAuthenticationFilter.java`
- Purpose: Intercepts HTTP requests and validates JWT tokens
- Functionality:
  - Extracts JWT from Authorization header
  - Validates token and sets authentication in SecurityContext
  - Converts roles to Spring Security authorities

#### SecurityConfig.java
- Location: `src/main/java/com/bookstore/config/SecurityConfig.java`
- Purpose: Configures Spring Security with JWT authentication
- Configuration:
  - Disables CSRF (using JWT tokens)
  - Permits all GET requests to `/api/books/**`
  - Requires authentication for PUT and DELETE requests
  - Stateless session management

### 3. Protected Endpoints

The following endpoints now require `ADMIN_ROLE` or `DEV_ROLE`:

#### PUT /api/books/{id}
```java
@PutMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<BookDTO>> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO)
```

#### DELETE /api/books/{id}
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id)
```

#### DELETE /api/books/{id}/hard
```java
@DeleteMapping("/{id}/hard")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<Void>> hardDeleteBook(@PathVariable Long id)
```

**Important**: The @PreAuthorize annotations use the full authority name including the "ROLE_" prefix that Spring Security adds.

### 4. Configuration (application.yml)
```yaml
jwt:
  secret: bookstore-secret-key-for-jwt-token-generation-must-be-at-least-256-bits
  expiration: 86400000  # 24 hours in milliseconds
```

## Testing the Implementation

### Step 1: Start Required Services
```bash
# Start auth-service (port 8086)
cd backend/auth-service
mvn spring-boot:run

# Start book-service (port 8081)
cd backend/book-service
mvn spring-boot:run
```

### Step 2: Register Users with Different Roles

**Note**: By default, new users are assigned the USER_ROLE. To assign ADMIN_ROLE or DEV_ROLE, you need to update the database directly or create an admin endpoint.

#### Register Regular User (Default)
```bash
curl -X POST http://localhost:8086/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@bookstore.com",
    "password": "user123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Manually Add Admin/Dev Roles (Database)
After registration, you can add additional roles via SQL:

```sql
-- Add ADMIN_ROLE to user
INSERT INTO user_roles (user_id, role)
VALUES ((SELECT id FROM users WHERE email = 'admin@bookstore.com'), 'ADMIN_ROLE');

-- Add DEV_ROLE to user
INSERT INTO user_roles (user_id, role)
VALUES ((SELECT id FROM users WHERE email = 'dev@bookstore.com'), 'DEV_ROLE');

-- A user can have multiple roles
INSERT INTO user_roles (user_id, role)
VALUES ((SELECT id FROM users WHERE email = 'superuser@bookstore.com'), 'ADMIN_ROLE'),
       ((SELECT id FROM users WHERE email = 'superuser@bookstore.com'), 'DEV_ROLE');
```

### Step 3: Login and Get JWT Tokens

#### Login as User
```bash
curl -X POST http://localhost:8086/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@bookstore.com",
    "password": "admin123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "admin@bookstore.com",
    "firstName": "Admin",
    "lastName": "User",
    "roles": ["ADMIN_ROLE", "DEV_ROLE"],
    "active": true,
    "createdAt": "2024-01-01T12:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

**Important**: Save the token from the response for subsequent requests. The token contains the user's roles with "ROLE_" prefix internally.

### Step 4: Test Public Endpoints (No Authentication Required)

#### Get All Books
```bash
curl -X GET http://localhost:8081/api/books
```

#### Get Book by ID
```bash
curl -X GET http://localhost:8081/api/books/1
```

#### Search Books
```bash
curl -X GET "http://localhost:8081/api/books/search?query=java"
```

### Step 5: Test Protected Endpoints

#### Test UPDATE with Admin Token (Should Succeed)
```bash
curl -X PUT http://localhost:8081/api/books/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE" \
  -d '{
    "title": "Updated Book Title",
    "author": "Updated Author",
    "isbn": "978-1234567890",
    "price": 29.99,
    "category": "Technology",
    "description": "Updated description"
  }'
```

Expected Response (200 OK):
```json
{
  "success": true,
  "message": "Book updated successfully",
  "data": {
    "id": 1,
    "title": "Updated Book Title",
    ...
  }
}
```

#### Test DELETE with Developer Token (Should Succeed)
```bash
curl -X DELETE http://localhost:8081/api/books/1 \
  -H "Authorization: Bearer YOUR_DEV_TOKEN_HERE"
```

Expected Response (200 OK):
```json
{
  "success": true,
  "message": "Book deleted successfully",
  "data": null
}
```

#### Test UPDATE without Token (Should Fail)
```bash
curl -X PUT http://localhost:8081/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Book Title",
    "author": "Updated Author"
  }'
```

Expected Response (403 Forbidden):
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/books/1"
}
```

#### Test UPDATE with Regular User Token (Should Fail)
```bash
curl -X PUT http://localhost:8081/api/books/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN_HERE" \
  -d '{
    "title": "Updated Book Title",
    "author": "Updated Author"
  }'
```

Expected Response (403 Forbidden):
```json
{
  "timestamp": "2024-01-01T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/books/1"
}
```

## Security Flow

1. **User Authentication**: User logs in via auth-service with email/password
2. **Token Generation**: Auth-service generates JWT token with user's roles (prefixed with "ROLE_")
3. **Token Inclusion**: Client includes JWT in Authorization header: `Bearer <token>`
4. **Token Validation**: JwtAuthenticationFilter validates token signature and expiration
5. **Role Extraction**: Filter extracts roles from token (e.g., "ROLE_ADMIN_ROLE", "ROLE_DEV_ROLE")
6. **Authority Mapping**: Roles are converted to Spring Security authorities
7. **Authorization Check**: @PreAuthorize annotation checks if user has required authority
8. **Access Decision**: Request is allowed or denied based on authority check

## Role Hierarchy

Users can have multiple roles simultaneously. The available roles are:

- **ADMIN_ROLE**: Full access to all operations (CREATE, READ, UPDATE, DELETE)
- **DEV_ROLE**: Full access to all operations (CREATE, READ, UPDATE, DELETE)
- **USER_ROLE**: Read-only access (GET operations only) - assigned by default on signup
- **Anonymous**: Read-only access (GET operations only)

**Note**: In the JWT token and Spring Security context, roles are stored with "ROLE_" prefix:
- Database: `ADMIN_ROLE` → JWT/Security: `ROLE_ADMIN_ROLE`
- Database: `DEV_ROLE` → JWT/Security: `ROLE_DEV_ROLE`
- Database: `USER_ROLE` → JWT/Security: `ROLE_USER_ROLE`

## Error Responses

### 401 Unauthorized
- Missing or invalid JWT token
- Token expired

### 403 Forbidden
- Valid token but insufficient permissions
- User role doesn't match required roles (ADMIN_ROLE or DEV_ROLE)

## Best Practices

1. **Token Storage**: Store JWT tokens securely (e.g., httpOnly cookies or secure storage)
2. **Token Expiration**: Tokens expire after 24 hours; implement refresh token mechanism
3. **HTTPS**: Always use HTTPS in production to protect tokens in transit
4. **Secret Key**: Use a strong, unique secret key (minimum 256 bits)
5. **Role Validation**: Always validate roles on the server side

## Troubleshooting

### Issue: 403 Forbidden even with valid token
- Check if user has ADMIN_ROLE or DEV_ROLE in the database (user_roles table)
- Verify token is not expired
- Ensure Authorization header format: `Bearer <token>`
- Confirm roles in JWT token have "ROLE_" prefix
- Check @PreAuthorize annotations use correct authority names (e.g., 'ROLE_ADMIN_ROLE')

### Issue: 401 Unauthorized
- Verify JWT secret matches between auth-service and book-service
- Check token expiration time
- Ensure token is properly formatted

### Issue: Token validation fails
- Verify both services use the same JWT secret
- Check for clock skew between services
- Ensure token hasn't been tampered with

## Additional Notes

- The same JWT secret must be configured in both auth-service and book-service
- Tokens are stateless; no server-side session storage required
- Role information is embedded in the JWT token with "ROLE_" prefix
- Users can have multiple roles stored in the user_roles table
- The User entity uses `@ElementCollection` to map the Set<UserRole> to the user_roles table
- Spring Security's `getAuthorities()` method automatically adds "ROLE_" prefix to role names
- Security configuration can be extended to protect other endpoints as needed

## Database Schema

### users table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### user_roles table
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Made with Bob