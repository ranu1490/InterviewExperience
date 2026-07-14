import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

declare const google: any;

/**
 * Loads Google Identity Services on demand and returns an ID token for the signed-in user.
 *
 * The browser obtains the ID token directly from Google; we then hand it to our backend
 * (`POST /api/auth/google`) which verifies it and issues our own JWTs. This is the recommended,
 * fully stateless SPA flow — no server-side redirect/callback URLs to manage.
 */
@Injectable({ providedIn: 'root' })
export class GoogleAuthService {
  private scriptLoaded = false;

  get isConfigured(): boolean {
    return !!environment.googleClientId;
  }

  /** Resolves with a Google ID token, or rejects if unconfigured/cancelled. */
  async getIdToken(): Promise<string> {
    if (!this.isConfigured) {
      throw new Error('Google login is not configured (set googleClientId in environment).');
    }
    await this.loadScript();
    return new Promise<string>((resolve, reject) => {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: { credential?: string }) => {
          if (response?.credential) {
            resolve(response.credential);
          } else {
            reject(new Error('No credential returned by Google'));
          }
        },
      });
      google.accounts.id.prompt();
    });
  }

  private loadScript(): Promise<void> {
    if (this.scriptLoaded) {
      return Promise.resolve();
    }
    return new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => {
        this.scriptLoaded = true;
        resolve();
      };
      script.onerror = () => reject(new Error('Failed to load Google Identity Services'));
      document.head.appendChild(script);
    });
  }
}
