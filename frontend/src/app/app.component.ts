import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <a routerLink="/" class="brand">
        <mat-icon>work_outline</mat-icon>
        <span>Interview Portal</span>
      </a>

      <nav class="nav-links">
        <a mat-button routerLink="/interviews" routerLinkActive="active">Browse</a>
        @if (auth.isAuthenticated()) {
          <a mat-button routerLink="/interviews/new" routerLinkActive="active">Share</a>
          <a mat-button routerLink="/my-interviews" routerLinkActive="active">My Posts</a>
        }
        @if (auth.isAdmin()) {
          <a mat-button routerLink="/admin" routerLinkActive="active">Admin</a>
        }
      </nav>

      <span class="spacer"></span>

      @if (auth.isAuthenticated()) {
        <button mat-button [matMenuTriggerFor]="menu">
          <mat-icon>account_circle</mat-icon>
          {{ auth.user()?.username }}
        </button>
        <mat-menu #menu="matMenu">
          <a mat-menu-item routerLink="/profile">
            <mat-icon>person</mat-icon><span>Profile</span>
          </a>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon><span>Logout</span>
          </button>
        </mat-menu>
      } @else {
        <a mat-button routerLink="/login">Login</a>
        <a mat-raised-button color="accent" routerLink="/signup">Sign up</a>
      }
    </mat-toolbar>

    <main class="app-content">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      .app-toolbar {
        position: sticky;
        top: 0;
        z-index: 10;
        gap: 8px;
      }
      .brand {
        display: flex;
        align-items: center;
        gap: 8px;
        color: inherit;
        text-decoration: none;
        font-weight: 600;
        margin-right: 16px;
      }
      .nav-links {
        display: flex;
        gap: 4px;
      }
      .nav-links .active {
        font-weight: 700;
      }
      .spacer {
        flex: 1 1 auto;
      }
      .app-content {
        max-width: 1100px;
        margin: 0 auto;
        padding: 24px 16px 64px;
      }
    `,
  ],
})
export class AppComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
