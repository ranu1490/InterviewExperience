import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { GoogleAuthService } from '../../core/services/google-auth.service';

@Component({
  selector: 'app-signup',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  template: `
    <div class="auth-wrap">
      <mat-card class="auth-card">
        @if (loading()) {
          <mat-progress-bar mode="indeterminate" />
        }
        <mat-card-header>
          <mat-card-title>Create your account</mat-card-title>
          <mat-card-subtitle>Join and share your interview journey</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field appearance="outline">
              <mat-label>Full name</mat-label>
              <input matInput formControlName="fullName" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Username</mat-label>
              <input matInput formControlName="username" />
              @if (form.controls.username.hasError('minlength')) {
                <mat-error>At least 3 characters</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" />
              @if (form.controls.email.hasError('email')) {
                <mat-error>Enter a valid email</mat-error>
              }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Password</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="new-password" />
              @if (form.controls.password.hasError('minlength')) {
                <mat-error>At least 8 characters</mat-error>
              }
            </mat-form-field>
            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading()">
              Sign up
            </button>
          </form>

          <button mat-stroked-button class="google-btn" (click)="google()" [disabled]="loading()">
            <mat-icon>login</mat-icon> Continue with Google
          </button>

          <p class="switch">Already have an account? <a routerLink="/login">Log in</a></p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .auth-wrap { display: flex; justify-content: center; padding-top: 24px; }
      .auth-card { width: 100%; max-width: 440px; }
      form { display: flex; flex-direction: column; gap: 4px; margin-top: 12px; }
      mat-form-field { width: 100%; }
      .google-btn { width: 100%; margin-top: 12px; }
      .switch { text-align: center; margin-top: 16px; }
    `,
  ],
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly googleAuth = inject(GoogleAuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);

  readonly form = this.fb.nonNullable.group({
    fullName: [''],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.auth.signup(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.loading.set(false),
    });
  }

  async google(): Promise<void> {
    try {
      this.loading.set(true);
      const idToken = await this.googleAuth.getIdToken();
      this.auth.googleLogin(idToken).subscribe({
        next: () => this.router.navigate(['/']),
        error: () => this.loading.set(false),
      });
    } catch (err) {
      this.loading.set(false);
      this.snackBar.open((err as Error).message, 'Dismiss', { duration: 5000 });
    }
  }
}
