import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../../core/services/book.service';
import { CartService } from '../../../core/services/cart.service';
import { Book } from '../../../core/models/book.model';
import { BookFormComponent } from '../book-form/book-form.component';
import { BookDeleteDialogComponent } from '../book-delete-dialog/book-delete-dialog.component';

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
    MatCardModule,
    MatPaginatorModule,
    MatDialogModule,
    MatSnackBarModule,
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
  totalPages = 0;

  displayedColumns: string[] = ['title', 'author', 'isbn', 'price', 'stockQuantity', 'actions'];

  constructor(
    private bookService: BookService,
    private cartService: CartService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
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
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load books. Please try again.';
        this.loading = false;
        console.error('Error loading books:', err);
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.currentPage = 0;
    this.loadBooks();
  }

  viewBook(id: number): void {
    this.router.navigate(['/books', id]);
  }

  addBook(): void {
    const dialogRef = this.dialog.open(BookFormComponent, {
      width: '800px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBooks();
      }
    });
  }

  editBook(book: Book): void {
    const dialogRef = this.dialog.open(BookFormComponent, {
      width: '800px',
      data: { mode: 'edit', book: book }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadBooks();
      }
    });
  }

  deleteBook(book: Book): void {
    const dialogRef = this.dialog.open(BookDeleteDialogComponent, {
      width: '400px',
      data: { book: book }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.bookService.deleteBook(book.id!).subscribe({
          next: () => {
            this.loadBooks();
          },
          error: (err) => {
            this.error = 'Failed to delete book. Please try again.';
            this.loading = false;
            console.error('Error deleting book:', err);
          }
        });
      }
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadBooks();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadBooks();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadBooks();
  }

  addToCart(book: Book): void {
    const cartItem = {
      bookId: book.id!,
      bookTitle: book.title,
      bookAuthor: book.author,
      bookIsbn: book.isbn,
      bookPrice: book.price,
      quantity: 1
    };

    this.cartService.addToCart(cartItem).subscribe({
      next: () => {
        this.snackBar.open(`"${book.title}" added to cart!`, 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.snackBar.open('Failed to add item to cart', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
      }
    });
  }
}

// Made with Bob
