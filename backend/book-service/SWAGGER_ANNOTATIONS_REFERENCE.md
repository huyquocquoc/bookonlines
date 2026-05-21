# Swagger/OpenAPI Annotations Reference

This document shows how to add OpenAPI annotations to the existing `BookController.java`. Copy these annotations to enhance your API documentation.

## Controller-Level Annotations

Add to the class declaration:

```java
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Books", description = "Book management APIs for the online bookstore")
@RestController
@RequestMapping("/api/books")
public class BookController {
    // ...
}
```

## Import Statements to Add

Add these imports at the top of BookController.java:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
```

## Method Annotations

### GET /api/books - Get All Books

```java
@Operation(
    summary = "Get all books",
    description = "Retrieve a paginated list of all active books in the system"
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved books"
    )
})
@GetMapping
public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooks(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "30")
        @RequestParam(defaultValue = "30") int size) {
    // existing code
}
```

### GET /api/books/{id} - Get Book by ID

```java
@Operation(
    summary = "Get book by ID",
    description = "Retrieve detailed information about a specific book"
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Book found"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Book not found"
    )
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<BookDTO>> getBookById(
        @Parameter(description = "Book ID", required = true, example = "1")
        @PathVariable Long id) {
    // existing code
}
```

### GET /api/books/search - Search Books

```java
@Operation(
    summary = "Search books",
    description = "Search for books by title, author, ISBN, or description"
)
@GetMapping("/search")
public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
        @Parameter(description = "Search query", required = true, example = "Java")
        @RequestParam String query,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "30")
        @RequestParam(defaultValue = "30") int size) {
    // existing code
}
```

### GET /api/books/category/{category} - Get Books by Category

```java
@Operation(
    summary = "Get books by category",
    description = "Retrieve all books in a specific category"
)
@GetMapping("/category/{category}")
public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByCategory(
        @Parameter(description = "Category name", required = true, example = "Programming")
        @PathVariable String category,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "30")
        @RequestParam(defaultValue = "30") int size) {
    // existing code
}
```

### GET /api/books/author/{author} - Get Books by Author

```java
@Operation(
    summary = "Get books by author",
    description = "Retrieve all books by a specific author"
)
@GetMapping("/author/{author}")
public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByAuthor(
        @Parameter(description = "Author name", required = true, example = "Robert C. Martin")
        @PathVariable String author,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "30")
        @RequestParam(defaultValue = "30") int size) {
    // existing code
}
```

### POST /api/books - Create Book

```java
@Operation(
    summary = "Create a new book",
    description = "Add a new book to the inventory. Requires authentication.",
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "Book created successfully"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid book data"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Unauthorized"
    )
})
@PostMapping
public ResponseEntity<ApiResponse<BookDTO>> createBook(
        @Parameter(description = "Book details", required = true)
        @Valid @RequestBody BookDTO bookDTO) {
    // existing code
}
```

### PUT /api/books/{id} - Update Book

```java
@Operation(
    summary = "Update an existing book",
    description = "Update book information. Requires ADMIN or DEV role.",
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Book updated successfully"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid book data"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Unauthorized"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Forbidden"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Book not found"
    )
})
@PutMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<BookDTO>> updateBook(
        @Parameter(description = "Book ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated book details", required = true)
        @Valid @RequestBody BookDTO bookDTO) {
    // existing code
}
```

### DELETE /api/books/{id} - Soft Delete

```java
@Operation(
    summary = "Delete a book (soft delete)",
    description = "Mark a book as inactive. Requires ADMIN or DEV role.",
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Book deleted successfully"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Unauthorized"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Forbidden"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Book not found"
    )
})
@DeleteMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<Void>> deleteBook(
        @Parameter(description = "Book ID", required = true, example = "1")
        @PathVariable Long id) {
    // existing code
}
```

### DELETE /api/books/{id}/hard - Hard Delete

```java
@Operation(
    summary = "Permanently delete a book",
    description = "Permanently remove a book from the database. Cannot be undone. Requires ADMIN or DEV role.",
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Book permanently deleted"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Unauthorized"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Forbidden"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Book not found"
    )
})
@DeleteMapping("/{id}/hard")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN_ROLE', 'ROLE_DEV_ROLE')")
public ResponseEntity<ApiResponse<Void>> hardDeleteBook(
        @Parameter(description = "Book ID", required = true, example = "1")
        @PathVariable Long id) {
    // existing code
}
```

### GET /api/books/count - Get Total Count

```java
@Operation(
    summary = "Get total count of active books",
    description = "Returns the total number of active books in the system"
)
@GetMapping("/count")
public ResponseEntity<ApiResponse<Long>> getTotalActiveBooks() {
    // existing code
}
```

## How to Apply

1. Open `BookController.java`
2. Add the import statements at the top
3. Add `@Tag` annotation to the class
4. Copy the annotations for each method above the existing method signatures
5. Keep all existing code unchanged

## Testing

After applying the annotations:

1. Start the book-service
2. Open browser to: http://localhost:8081/swagger-ui.html
3. You should see all endpoints documented with descriptions
4. Click "Authorize" to add JWT token for protected endpoints
5. Try out the endpoints directly from Swagger UI

## Notes

- The annotations are purely for documentation and don't change functionality
- All existing code remains the same
- Swagger UI will automatically generate interactive documentation
- The OpenAPI spec can be downloaded at: http://localhost:8081/api-docs