import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { ShoppingCartComponent } from './shared/components/shopping-cart/shopping-cart.component';
import { AuthService } from './core/services/auth.service';
import { User } from './core/models/auth.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    ShoppingCartComponent
  ],
  template: `
    <mat-toolbar color="primary" *ngIf="currentUser">
      <mat-icon>book</mat-icon>
      <span style="margin-left: 10px;">Book Management System</span>
      <span style="flex: 1 1 auto;"></span>
      <button mat-button routerLink="/books">
        <mat-icon>list</mat-icon>
        Books
      </button>
      <app-shopping-cart></app-shopping-cart>
      <button mat-button [matMenuTriggerFor]="userMenu">
        <mat-icon>account_circle</mat-icon>
        {{ currentUser.firstName }}
      </button>
      <mat-menu #userMenu="matMenu">
        <div class="user-info" (click)="$event.stopPropagation()">
          <p><strong>{{ currentUser.firstName }} {{ currentUser.lastName }}</strong></p>
          <p class="user-email">{{ currentUser.email }}</p>
        </div>
        <mat-divider></mat-divider>
        <button mat-menu-item (click)="logout()">
          <mat-icon>logout</mat-icon>
          Logout
        </button>
      </mat-menu>
    </mat-toolbar>
    
    <div class="container" [class.no-toolbar]="!currentUser">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .container.no-toolbar {
      padding: 0;
    }
    
    mat-toolbar {
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .user-info {
      padding: 16px;
      min-width: 200px;
    }

    .user-info p {
      margin: 4px 0;
    }

    .user-email {
      font-size: 12px;
      color: #666;
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'Book Management System';
  currentUser: User | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout(): void {
    this.authService.logout();
  }
}

// Made with Bob
