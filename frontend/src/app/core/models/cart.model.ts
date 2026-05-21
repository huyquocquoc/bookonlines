export interface CartItem {
  id?: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookIsbn: string;
  bookPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  id?: number;
  sessionId: string;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
}

export interface CheckoutSessionRequest {
  successUrl?: string;
  cancelUrl?: string;
}

export interface CheckoutSessionResponse {
  sessionId: string;
  checkoutUrl: string;
}

// Made with Bob
