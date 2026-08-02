import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatChipsModule,
  ],
  template: `
    <h1>My profile</h1>
    <mat-card class="card">
      <mat-card-content>
        <div class="identity">
          <p><strong>Username:</strong> {{ auth.user()?.username }}</p>
          <p><strong>Email:</strong> {{ auth.user()?.email }}</p>
          <p>
            <strong>Roles:</strong>
            @for (r of auth.user()?.roles; track r) { <mat-chip>{{ r }}</mat-chip> }
          </p>
        </div>

        <form [formGroup]="form" (ngSubmit)="save()">
          <mat-form-field appearance="outline" class="full">
            <mat-label>Full name</mat-label>
            <input matInput formControlName="fullName" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Bio</mat-label>
            <textarea matInput rows="3" formControlName="bio"></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Avatar URL</mat-label>
            <input matInput formControlName="avatarUrl" />
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="saving()">Save</button>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [
    `
      .card { max-width: 560px; }
      .identity { margin-bottom: 16px; }
      .identity p { margin: 4px 0; }
      form { display: flex; flex-direction: column; gap: 4px; }
      .full { width: 100%; }
    `,
  ],
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    fullName: [''],
    bio: [''],
    avatarUrl: [''],
  });

  ngOnInit(): void {
    const user = this.auth.user();
    this.form.patchValue({
      fullName: user?.fullName ?? '',
      bio: user?.bio ?? '',
      avatarUrl: user?.avatarUrl ?? '',
    });
  }

  save(): void {
    this.saving.set(true);
    this.auth.updateProfile(this.form.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Profile updated', 'OK', { duration: 3000 });
      },
      error: () => this.saving.set(false),
    });
  }
}
