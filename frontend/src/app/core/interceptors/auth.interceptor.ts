import {
  HttpClient,
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/user.model';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

// Shared across invocations so concurrent 401s trigger only ONE refresh call.
let refreshing = false;
const refreshedToken$ = new BehaviorSubject<string | null>(null);

/**
 * Attaches the access token to outgoing requests and transparently refreshes it on a 401.
 *
 * The refresh is single-flight: the first 401 kicks off a refresh, and any other requests that
 * 401 in the meantime wait for that same refresh to finish, then retry. This prevents a burst of
 * refresh calls (and refresh-token rotation races) when a token expires mid-session.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);
  const http = inject(HttpClient);

  const isAuthCall = req.url.includes('/auth/');
  const token = tokenService.accessToken;
  const authReq = token && !isAuthCall ? addToken(req, token) : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthCall && tokenService.refreshToken) {
        return handle401(req, next, tokenService, authService, http);
      }
      return throwError(() => error);
    }),
  );
};

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

function handle401(
  req: HttpRequest<unknown>,
  next: (r: HttpRequest<unknown>) => Observable<any>,
  tokenService: TokenService,
  authService: AuthService,
  http: HttpClient,
): Observable<any> {
  if (refreshing) {
    return refreshedToken$.pipe(
      filter((t): t is string => t !== null),
      take(1),
      switchMap((t) => next(addToken(req, t))),
    );
  }

  refreshing = true;
  refreshedToken$.next(null);

  return http
    .post<AuthResponse>(`${environment.apiUrl}/auth/refresh-token`, {
      refreshToken: tokenService.refreshToken,
    })
    .pipe(
      switchMap((res) => {
        tokenService.setTokens(res.accessToken, res.refreshToken);
        refreshing = false;
        refreshedToken$.next(res.accessToken);
        return next(addToken(req, res.accessToken));
      }),
      catchError((err) => {
        refreshing = false;
        authService.logout();
        return throwError(() => err);
      }),
    );
}
