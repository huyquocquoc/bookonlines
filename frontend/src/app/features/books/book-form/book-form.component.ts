import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BookService } from '../../../core/services/book.service';
import { Book } from '../../../core/models/book.model';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './book-form.component.html',
  styleUrls: ['./book-form.component.css']
})
export class BookFormComponent implements OnInit {
  bookForm!: FormGroup;
  isEditMode = false;
  bookId?: number;
  loading = false;
  submitting = false;

  categories = [
    'Fiction',
    'Non-Fiction',
    'Science Fiction',
    'Fantasy',
    'Mystery',
    'Thriller',
    'Romance',
    'Biography',
    'History',
    'Science',
    'Technology',
    'Business',
    'Self-Help',
    'Children',
    'Young Adult',
    'Poetry',
    'Drama',
    'Horror',
    'Adventure',
    'Other'
  ];

  languages = [
    'English',
    'Spanish',
    'French',
    'German',
    'Italian',
    'Portuguese',
    'Chinese',
    'Japanese',
    'Korean',
    'Arabic',
    'Russian',
    'Other'
  ];

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.bookId = +params['id'];
        this.loadBook(this.bookId);
      }
    });
  }

  private initForm(): void {
    this.bookForm = this.fb.group({
      isbn: ['', [Validators.required, Validators.pattern(/^(?:\d{10}|\d{13})$/)]],
      title: ['', [Validators.required, Validators.maxLength(255)]],
      author: ['', [Validators.required, Validators.maxLength(255)]],
      publisher: ['', Validators.maxLength(255)],
      category: ['', Validators.required],
      language: ['', Validators.required],
      description: ['', Validators.maxLength(2000)],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      publishedDate: [''],
      pageCount: [0, Validators.min(0)],
      coverImageUrl: ['', Validators.maxLength(500)],
      rating: [0, [Validators.min(0), Validators.max(5)]],
      active: [true]
    });
  }

  private loadBook(id: number): void {
    this.loading = true;
    this.bookService.getBookById(id).subscribe({
      next: (book) => {
        this.bookForm.patchValue({
          isbn: book.isbn,
          title: book.title,
          author: book.author,
          publisher: book.publisher,
          category: book.category,
          language: book.language,
          description: book.description,
          price: book.price,
          stockQuantity: book.stockQuantity,
          publishedDate: book.publishedDate ? new Date(book.publishedDate) : null,
          pageCount: book.pageCount,
          coverImageUrl: book.coverImageUrl,
          rating: book.rating,
          active: book.active
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading book:', error);
        this.snackBar.open('Failed to load book details', 'Close', { duration: 3000 });
        this.loading = false;
        this.router.navigate(['/books']);
      }
    });
  }

  onSubmit(): void {
    if (this.bookForm.valid) {
      this.submitting = true;
      const bookData: Partial<Book> = this.bookForm.value;

      // Format date if present
      if (bookData.publishedDate) {
        bookData.publishedDate = new Date(bookData.publishedDate).toISOString().split('T')[0];
      }

      const operation = this.isEditMode && this.bookId
        ? this.bookService.updateBook(this.bookId, bookData)
        : this.bookService.createBook(bookData);

      operation.subscribe({
        next: (response) => {
          this.snackBar.open(
            this.isEditMode ? 'Book updated successfully' : 'Book created successfully',
            'Close',
            { duration: 3000 }
          );
          this.submitting = false;
          this.router.navigate(['/books']);
        },
        error: (error) => {
          console.error('Error saving book:', error);
          this.snackBar.open(
            error.error?.message || 'Failed to save book',
            'Close',
            { duration: 3000 }
          );
          this.submitting = false;
        }
      });
    } else {
      this.markFormGroupTouched(this.bookForm);
      this.snackBar.open('Please fill in all required fields correctly', 'Close', { duration: 3000 });
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    this.router.navigate(['/books']);
  }

  getErrorMessage(fieldName: string): string {
    const control = this.bookForm.get(fieldName);
    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('pattern')) {
      return 'Invalid format';
    }
    if (control?.hasError('min')) {
      return `Minimum value is ${control.errors?.['min'].min}`;
    }
    if (control?.hasError('max')) {
      return `Maximum value is ${control.errors?.['max'].max}`;
    }
    if (control?.hasError('maxlength')) {
      return `Maximum length is ${control.errors?.['maxlength'].requiredLength}`;
    }
    return '';
  }
}

// Made with Bob
