import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/**
 * Surfaces API errors to the user as a snackbar, using the backend's uniform error body
 * ({@code {message, fieldErrors, ...}}) when available. Keeps components from each having to
 * repeat error-toast boilerplate. 401s are swallowed here (the auth interceptor handles them).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401) {
        snackBar.open(extractMessage(error), 'Dismiss', { duration: 5000 });
      }
      return throwError(() => error);
    }),
  );
};

function extractMessage(error: HttpErrorResponse): string {
  if (error.error?.message) {
    return error.error.message;
  }
  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running?';
  }
  return `Request failed (${error.status})`;
}
