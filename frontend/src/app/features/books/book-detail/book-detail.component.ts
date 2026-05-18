import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BookService } from '../../../core/services/book.service';
import { Book } from '../../../core/models/book.model';
import { BookFormComponent } from '../book-form/book-form.component';
import { BookDeleteDialogComponent } from '../book-delete-dialog/book-delete-dialog.component';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatDialogModule
  ],
  templateUrl: './book-detail.component.html',
  styleUrls: ['./book-detail.component.css']
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBook(+id);
    }
  }

  loadBook(id: number): void {
    this.loading = true;
    this.error = null;

    this.bookService.getBookById(id).subscribe({
      next: (book) => {
        this.book = book;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load book details. Please try again.';
        this.loading = false;
        console.error('Error loading book:', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/books']);
  }

  editBook(): void {
    if (!this.book) return;

    const dialogRef = this.dialog.open(BookFormComponent, {
      width: '800px',
      data: { mode: 'edit', book: this.book }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.book) {
        this.loadBook(this.book.id!);
      }
    });
  }

  deleteBook(): void {
    if (!this.book) return;

    const dialogRef = this.dialog.open(BookDeleteDialogComponent, {
      width: '400px',
      data: { book: this.book }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.bookService.deleteBook(this.book!.id!).subscribe({
          next: () => {
            this.router.navigate(['/books']);
          },
          error: (err) => {
            this.error = 'Failed to delete book. Please try again.';
            console.error('Error deleting book:', err);
          }
        });
      }
    });
  }
}

// Made with Bob
