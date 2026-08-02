import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, MatButtonModule],
  template: `
    <div class="nf">
      <h1>404</h1>
      <p>We couldn't find that page.</p>
      <a mat-raised-button color="primary" routerLink="/">Back home</a>
    </div>
  `,
  styles: [
    `
      .nf { text-align: center; padding: 80px 16px; }
      .nf h1 { font-size: 4rem; margin: 0; }
      .nf p { color: rgba(0, 0, 0, 0.6); margin-bottom: 24px; }
    `,
  ],
})
export class NotFoundComponent {}
