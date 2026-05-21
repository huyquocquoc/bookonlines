import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CartService } from '../../../core/services/cart.service';
import { Cart } from '../../../core/models/cart.model';

@Component({
  selector: 'app-shopping-cart',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    MatMenuModule,
    MatListModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './shopping-cart.component.html',
  styleUrls: ['./shopping-cart.component.css']
})
export class ShoppingCartComponent implements OnInit {
  cart: Cart | null = null;
  cartItemCount = 0;
  checkoutInProgress = false;

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
      this.cartItemCount = cart ? cart.totalItems : 0;
    });
  }

  removeItem(itemId: number): void {
    if (itemId) {
      this.cartService.removeFromCart(itemId).subscribe({
        next: () => {
          console.log('Item removed from cart');
        },
        error: (error) => {
          console.error('Error removing item:', error);
        }
      });
    }
  }

  clearCart(): void {
    if (confirm('Are you sure you want to clear the cart?')) {
      this.cartService.clearCart().subscribe({
        next: () => {
          console.log('Cart cleared');
        },
        error: (error) => {
          console.error('Error clearing cart:', error);
        }
      });
    }
  }

  checkout(): void {
    if (!this.cart || this.cart.items.length === 0 || this.checkoutInProgress) {
      return;
    }

    this.checkoutInProgress = true;

    const origin = window.location.origin;
    this.cartService.createCheckoutSession({
      successUrl: `${origin}/?checkout=success`,
      cancelUrl: `${origin}/?checkout=cancelled`
    }).subscribe({
      next: (response) => {
        window.location.href = response.checkoutUrl;
      },
      error: (error) => {
        this.checkoutInProgress = false;
        console.error('Error creating checkout session:', error);
        alert('Unable to start Stripe checkout. Please try again.');
      }
    });
  }
}

// Made with Bob
