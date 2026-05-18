import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Book, PageResponse, ApiResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiUrl = '/api/books';

  constructor(private http: HttpClient) {}

  /**
   * Get all books with pagination
   */
  getBooks(page: number = 0, size: number = 30): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<Book>>>(this.apiUrl, { params })
      .pipe(map(response => response.data));
  }

  /**
   * Get book by ID
   */
  getBookById(id: number): Observable<Book> {
    return this.http.get<ApiResponse<Book>>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  /**
   * Search books
   */
  searchBooks(query: string, page: number = 0, size: number = 30): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<Book>>>(`${this.apiUrl}/search`, { params })
      .pipe(map(response => response.data));
  }

  /**
   * Get books by category
   */
  getBooksByCategory(category: string, page: number = 0, size: number = 30): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<Book>>>(`${this.apiUrl}/category/${category}`, { params })
      .pipe(map(response => response.data));
  }

  /**
   * Get books by author
   */
  getBooksByAuthor(author: string, page: number = 0, size: number = 30): Observable<PageResponse<Book>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<Book>>>(`${this.apiUrl}/author/${author}`, { params })
      .pipe(map(response => response.data));
  }

  /**
   * Create new book
   */
  createBook(book: Partial<Book>): Observable<Book> {
    return this.http.post<ApiResponse<Book>>(this.apiUrl, book)
      .pipe(map(response => response.data));
  }

  /**
   * Update existing book
   */
  updateBook(id: number, book: Partial<Book>): Observable<Book> {
    return this.http.put<ApiResponse<Book>>(`${this.apiUrl}/${id}`, book)
      .pipe(map(response => response.data));
  }

  /**
   * Delete book
   */
  deleteBook(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`)
      .pipe(map(() => undefined));
  }

  /**
   * Get total count of active books
   */
  getTotalActiveBooks(): Observable<number> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/count`)
      .pipe(map(response => response.data));
  }
}

// Made with Bob
