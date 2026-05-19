import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/books',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'books',
    loadComponent: () => import('./features/books/book-list/book-list.component')
      .then(m => m.BookListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'books/:id',
    loadComponent: () => import('./features/books/book-detail/book-detail.component')
      .then(m => m.BookDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/books'
  }
];

// Made with Bob
