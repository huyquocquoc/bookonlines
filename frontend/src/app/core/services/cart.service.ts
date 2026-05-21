import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Cart, CartItem, CheckoutSessionRequest, CheckoutSessionResponse } from '../models/cart.model';
import { ApiResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = 'http://localhost:8084/api/cart';
  private cartSubject = new BehaviorSubject<Cart | null>(null);
  public cart$ = this.cartSubject.asObservable();
  private sessionId: string = '';

  constructor(private http: HttpClient) {
    this.initializeSession();
  }

  /**
   * Initialize or retrieve session ID from localStorage
   */
  private initializeSession(): void {
    const storedSessionId = localStorage.getItem('cartSessionId');
    if (storedSessionId) {
      this.sessionId = storedSessionId;
      this.loadCart();
    } else {
      this.generateNewSession();
    }
  }

  /**
   * Generate a new session ID
   */
  private generateNewSession(): void {
    this.http.get<ApiResponse<string>>(`${this.apiUrl}/session/new`)
      .pipe(
        map(response => response.data)
      )
      .subscribe({
        next: (sessionId) => {
          this.sessionId = sessionId;
          localStorage.setItem('cartSessionId', sessionId);
          this.loadCart();
        },
        error: (error) => {
          console.error('Error generating session ID:', error);
          // Fallback to client-generated UUID
          this.sessionId = this.generateUUID();
          localStorage.setItem('cartSessionId', this.sessionId);
          this.loadCart();
        }
      });
  }

  /**
   * Generate UUID on client side as fallback
   */
  private generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  /**
   * Load cart from backend
   */
  loadCart(): void {
    if (!this.sessionId) return;

    this.http.get<ApiResponse<Cart>>(`${this.apiUrl}/${this.sessionId}`)
      .pipe(
        map(response => response.data)
      )
      .subscribe({
        next: (cart) => {
          this.cartSubject.next(cart);
        },
        error: (error) => {
          console.error('Error loading cart:', error);
        }
      });
  }

  /**
   * Add item to cart
   */
  addToCart(item: Omit<CartItem, 'id' | 'subtotal'>): Observable<Cart> {
    return this.http.post<ApiResponse<Cart>>(
      `${this.apiUrl}/${this.sessionId}/items`,
      item
    ).pipe(
      map(response => response.data),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  /**
   * Update item quantity
   */
  updateItemQuantity(itemId: number, quantity: number): Observable<Cart> {
    return this.http.put<ApiResponse<Cart>>(
      `${this.apiUrl}/${this.sessionId}/items/${itemId}`,
      null,
      { params: { quantity: quantity.toString() } }
    ).pipe(
      map(response => response.data),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  /**
   * Remove item from cart
   */
  removeFromCart(itemId: number): Observable<Cart> {
    return this.http.delete<ApiResponse<Cart>>(
      `${this.apiUrl}/${this.sessionId}/items/${itemId}`
    ).pipe(
      map(response => response.data),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  /**
   * Clear cart
   */
  clearCart(): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/${this.sessionId}`
    ).pipe(
      map(response => response.data),
      tap(() => this.cartSubject.next(null))
    );
  }

  createCheckoutSession(request: CheckoutSessionRequest): Observable<CheckoutSessionResponse> {
    return this.http.post<ApiResponse<CheckoutSessionResponse>>(
      `${this.apiUrl}/${this.sessionId}/checkout`,
      request
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get current cart value
   */
  getCurrentCart(): Cart | null {
    return this.cartSubject.value;
  }

  /**
   * Get cart item count
   */
  getCartItemCount(): number {
    const cart = this.cartSubject.value;
    return cart ? cart.totalItems : 0;
  }

  getSessionId(): string {
    return this.sessionId;
  }
}

// Made with Bob
