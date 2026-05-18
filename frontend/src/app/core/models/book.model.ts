export interface Book {
  id?: number;
  isbn: string;
  title: string;
  author: string;
  publisher?: string;
  category?: string;
  language?: string;
  description?: string;
  price: number;
  stockQuantity: number;
  publishedDate?: string;
  pageCount?: number;
  coverImageUrl?: string;
  rating?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiResponse<T> {
  status: string;
  data: T;
  message?: string;
  timestamp: string;
}

// Made with Bob
