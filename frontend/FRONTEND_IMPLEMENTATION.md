# Angular 18 Frontend Implementation Guide

## Project Structure Created

```
frontend/
├── package.json                    # Dependencies and scripts
├── angular.json                    # Angular CLI configuration
├── tsconfig.json                   # TypeScript configuration
├── tsconfig.app.json              # App-specific TypeScript config
├── tsconfig.spec.json             # Test TypeScript config
├── proxy.conf.json                # API proxy configuration
├── src/
│   ├── index.html                 # Main HTML file
│   ├── main.ts                    # Application bootstrap
│   ├── styles.css                 # Global styles
│   └── app/
│       ├── app.component.ts       # Root component
│       ├── app.config.ts          # Application configuration
│       ├── app.routes.ts          # Route definitions
│       └── core/
│           ├── models/
│           │   └── book.model.ts  # Book interfaces
│           └── services/
│               └── book.service.ts # Book API service
```

## Installation Steps

### 1. Install Dependencies

```bash
cd frontend
npm install
```

This will install:
- Angular 18 framework
- Angular Material UI components
- Angular CDK (Component Dev Kit) for virtual scrolling
- RxJS for reactive programming
- TypeScript and build tools

### 2. Components to Create

You need to create the following components (files are referenced but not yet created):

#### A. Book List Component (with Virtual Scrolling)
**Path**: `src/app/features/books/book-list/book-list.component.ts`

Features:
- Display books in a paginated table/grid
- Virtual scrolling for large datasets (>30 rows)
- Search functionality
- Filter by category/author
- Add/Edit/Delete buttons
- Loading states

#### B. Book Detail Component
**Path**: `src/app/features/books/book-detail/book-detail.component.ts`

Features:
- Display full book information
- Edit button
- Delete button
- Back navigation

#### C. Book Form Component
**Path**: `src/app/features/books/book-form/book-form.component.ts`

Features:
- Reactive form with validation
- Create/Edit modes
- All book fields
- Form validation
- Submit/Cancel buttons

#### D. Delete Confirmation Dialog
**Path**: `src/app/features/books/book-delete-dialog/book-delete-dialog.component.ts`

Features:
- Material Dialog
- Confirmation message
- Confirm/Cancel buttons

## Key Features Implemented

### 1. Book Service (✅ Complete)
Located at: `src/app/core/services/book.service.ts`

Methods:
- `getBooks(page, size)` - Get paginated books
- `getBookById(id)` - Get single book
- `searchBooks(query, page, size)` - Search books
- `getBooksByCategory(category, page, size)` - Filter by category
- `getBooksByAuthor(author, page, size)` - Filter by author
- `createBook(book)` - Create new book
- `updateBook(id, book)` - Update book
- `deleteBook(id)` - Delete book
- `getTotalActiveBooks()` - Get count

### 2. Models (✅ Complete)
Located at: `src/app/core/models/book.model.ts`

Interfaces:
- `Book` - Book entity
- `PageResponse<T>` - Paginated response
- `ApiResponse<T>` - API response wrapper

### 3. Routing (✅ Complete)
Located at: `src/app/app.routes.ts`

Routes:
- `/` → Redirects to `/books`
- `/books` → Book list (lazy loaded)
- `/books/:id` → Book detail (lazy loaded)

### 4. Proxy Configuration (✅ Complete)
Located at: `proxy.conf.json`

Proxies `/api` requests to `http://localhost:8080`

## Running the Application

### Development Server

```bash
npm start
```

Access at: `http://localhost:4200`

The proxy will forward API calls to the backend at `http://localhost:8080`

### Build for Production

```bash
npm run build
```

Output will be in `dist/book-management-app/`

## Next Steps to Complete Frontend

### Step 1: Create Book List Component with Virtual Scrolling

```typescript
// src/app/features/books/book-list/book-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../../core/services/book.service';
import { Book } from '../../../core/models/book.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule,
    ScrollingModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    FormsModule
  ],
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.css']
})
export class BookListComponent implements OnInit {
  books: Book[] = [];
  loading = false;
  error: string | null = null;
  searchQuery = '';
  currentPage = 0;
  pageSize = 30;
  totalElements = 0;

  displayedColumns: string[] = ['title', 'author', 'isbn', 'price', 'stockQuantity', 'actions'];

  constructor(
    private bookService: BookService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading = true;
    this.error = null;

    const request = this.searchQuery
      ? this.bookService.searchBooks(this.searchQuery, this.currentPage, this.pageSize)
      : this.bookService.getBooks(this.currentPage, this.pageSize);

    request.subscribe({
      next: (response) => {
        this.books = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load books';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  viewBook(id: number): void {
    this.router.navigate(['/books', id]);
  }

  editBook(id: number): void {
    // Navigate to edit form
  }

  deleteBook(id: number): void {
    // Open delete confirmation dialog
  }

  nextPage(): void {
    this.currentPage++;
    this.loadBooks();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadBooks();
    }
  }
}
```

### Step 2: Create Book List Template

```html
<!-- src/app/features/books/book-list/book-list.component.html -->
<div class="book-list-container">
  <div class="header">
    <h1>Books</h1>
    <button mat-raised-button color="primary" (click)="addBook()">
      <mat-icon>add</mat-icon>
      Add Book
    </button>
  </div>

  <mat-form-field class="search-field">
    <mat-label>Search books</mat-label>
    <input matInput [(ngModel)]="searchQuery" (keyup.enter)="onSearch()" placeholder="Search by title, author, or ISBN">
    <button mat-icon-button matSuffix (click)="onSearch()">
      <mat-icon>search</mat-icon>
    </button>
  </mat-form-field>

  <div *ngIf="loading" class="loading-container">
    <mat-spinner></mat-spinner>
  </div>

  <div *ngIf="error" class="error-message">
    {{ error }}
  </div>

  <div *ngIf="!loading && !error">
    <cdk-virtual-scroll-viewport itemSize="50" class="book-viewport">
      <table mat-table [dataSource]="books">
        <ng-container matColumnDef="title">
          <th mat-header-cell *matHeaderCellDef>Title</th>
          <td mat-cell *matCellDef="let book">{{ book.title }}</td>
        </ng-container>

        <ng-container matColumnDef="author">
          <th mat-header-cell *matHeaderCellDef>Author</th>
          <td mat-cell *matCellDef="let book">{{ book.author }}</td>
        </ng-container>

        <ng-container matColumnDef="isbn">
          <th mat-header-cell *matHeaderCellDef>ISBN</th>
          <td mat-cell *matCellDef="let book">{{ book.isbn }}</td>
        </ng-container>

        <ng-container matColumnDef="price">
          <th mat-header-cell *matHeaderCellDef>Price</th>
          <td mat-cell *matCellDef="let book">{{ book.price | currency }}</td>
        </ng-container>

        <ng-container matColumnDef="stockQuantity">
          <th mat-header-cell *matHeaderCellDef>Stock</th>
          <td mat-cell *matCellDef="let book">{{ book.stockQuantity }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let book">
            <button mat-icon-button (click)="viewBook(book.id!)">
              <mat-icon>visibility</mat-icon>
            </button>
            <button mat-icon-button (click)="editBook(book.id!)">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteBook(book.id!)">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </cdk-virtual-scroll-viewport>

    <div class="pagination">
      <button mat-button (click)="previousPage()" [disabled]="currentPage === 0">
        Previous
      </button>
      <span>Page {{ currentPage + 1 }}</span>
      <button mat-button (click)="nextPage()">
        Next
      </button>
    </div>
  </div>
</div>
```

## Material Design Components Used

- `MatToolbarModule` - Top navigation bar
- `MatButtonModule` - Buttons
- `MatIconModule` - Material icons
- `MatTableModule` - Data tables
- `MatFormFieldModule` - Form fields
- `MatInputModule` - Input fields
- `MatDialogModule` - Dialogs
- `MatProgressSpinnerModule` - Loading spinners
- `MatCardModule` - Cards
- `ScrollingModule` - Virtual scrolling

## API Integration

All API calls go through the `BookService` which:
1. Makes HTTP requests to `/api/books`
2. Handles responses with RxJS observables
3. Maps API responses to TypeScript models
4. Provides error handling

## Virtual Scrolling Implementation

Uses Angular CDK's `cdk-virtual-scroll-viewport`:
- Renders only visible items
- Efficient for large datasets (>30 rows)
- Smooth scrolling experience
- Automatic item recycling

## Form Validation

Reactive forms with validators:
- Required fields
- ISBN format
- Price validation (positive numbers)
- Stock quantity (non-negative)
- Date validation

## Error Handling

- HTTP interceptors for global error handling
- User-friendly error messages
- Loading states
- Retry logic for failed requests

## Responsive Design

- Mobile-first approach
- Flexbox layouts
- Material Design breakpoints
- Responsive tables

## Testing

Run tests:
```bash
npm test
```

## Troubleshooting

### Common Issues

1. **Module not found errors**: Run `npm install`
2. **Port already in use**: Change port in `angular.json`
3. **API connection failed**: Ensure backend is running on port 8080
4. **CORS errors**: Check backend CORS configuration

## Performance Optimization

- Lazy loading routes
- Virtual scrolling for large lists
- OnPush change detection strategy
- HTTP caching
- Debounced search

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [RxJS Documentation](https://rxjs.dev/)