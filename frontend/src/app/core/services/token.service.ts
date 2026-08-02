import { Injectable } from '@angular/core';
import { User } from '../models/user.model';

/**
 * Thin wrapper over localStorage for the JWT pair and cached user.
 *
 * Isolating storage access here means the rest of the app never touches localStorage keys
 * directly, so we could later switch to sessionStorage or in-memory storage in one place.
 */
@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly ACCESS = 'iep_access_token';
  private readonly REFRESH = 'iep_refresh_token';
  private readonly USER = 'iep_user';

  get accessToken(): string | null {
    return localStorage.getItem(this.ACCESS);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(this.REFRESH);
  }

  get user(): User | null {
    const raw = localStorage.getItem(this.USER);
    return raw ? (JSON.parse(raw) as User) : null;
  }

  setSession(accessToken: string, refreshToken: string, user: User): void {
    localStorage.setItem(this.ACCESS, accessToken);
    localStorage.setItem(this.REFRESH, refreshToken);
    localStorage.setItem(this.USER, JSON.stringify(user));
  }

  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.ACCESS, accessToken);
    localStorage.setItem(this.REFRESH, refreshToken);
  }

  clear(): void {
    localStorage.removeItem(this.ACCESS);
    localStorage.removeItem(this.REFRESH);
    localStorage.removeItem(this.USER);
  }
}
