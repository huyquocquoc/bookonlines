import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/books',
    pathMatch: 'full'
  },
  {
    path: 'books',
    loadComponent: () => import('./features/books/book-list/book-list.component')
      .then(m => m.BookListComponent)
  },
  {
    path: 'books/:id',
    loadComponent: () => import('./features/books/book-detail/book-detail.component')
      .then(m => m.BookDetailComponent)
  },
  {
    path: '**',
    redirectTo: '/books'
  }
];

// Made with Bob
