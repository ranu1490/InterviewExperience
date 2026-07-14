import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  SignupRequest,
  UpdateProfileRequest,
  User,
} from '../models/user.model';
import { TokenService } from './token.service';

/**
 * Central authentication state and API calls.
 *
 * Exposes the current user as a signal so templates react automatically to login/logout without
 * manual subscriptions — the modern Angular approach.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);

  private readonly baseUrl = `${environment.apiUrl}/auth`;
  private readonly usersUrl = `${environment.apiUrl}/users`;

  private readonly currentUser = signal<User | null>(this.tokenService.user);

  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.roles.includes('ADMIN') ?? false);

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/signup`, request)
      .pipe(tap((res) => this.storeSession(res)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(tap((res) => this.storeSession(res)));
  }

  googleLogin(idToken: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/google`, { idToken })
      .pipe(tap((res) => this.storeSession(res)));
  }

  updateProfile(request: UpdateProfileRequest): Observable<User> {
    return this.http
      .put<User>(`${this.usersUrl}/me`, request)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  logout(): void {
    const refreshToken = this.tokenService.refreshToken;
    if (refreshToken) {
      // Fire-and-forget: revoke the refresh token server-side; clear locally regardless.
      this.http.post(`${this.baseUrl}/logout`, { refreshToken }).subscribe({
        error: () => {},
      });
    }
    this.tokenService.clear();
    this.currentUser.set(null);
  }

  private storeSession(res: AuthResponse): void {
    this.tokenService.setSession(res.accessToken, res.refreshToken, res.user);
    this.currentUser.set(res.user);
  }
}
