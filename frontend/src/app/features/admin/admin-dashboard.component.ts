import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { Report } from '../../core/models/interview.model';
import { AdminService } from '../../core/services/admin.service';

/**
 * Admin moderation console: review the report queue, resolve/dismiss reports, delete offending
 * posts and ban users.
 */
@Component({
  selector: 'app-admin-dashboard',
  imports: [
    FormsModule,
    RouterLink,
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <h1>Admin dashboard</h1>

    <mat-card class="tools">
      <mat-form-field appearance="outline">
        <mat-label>Filter reports by status</mat-label>
        <mat-select [(ngModel)]="statusFilter" (selectionChange)="load()">
          <mat-option value="">All</mat-option>
          <mat-option value="PENDING">Pending</mat-option>
          <mat-option value="REVIEWED">Reviewed</mat-option>
          <mat-option value="DISMISSED">Dismissed</mat-option>
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Ban user by ID</mat-label>
        <input matInput type="number" [(ngModel)]="banUserId" />
      </mat-form-field>
      <button mat-stroked-button color="warn" (click)="ban()"><mat-icon>block</mat-icon> Ban</button>
      <button mat-stroked-button (click)="unban()"><mat-icon>check</mat-icon> Unban</button>
    </mat-card>

    <h2>Reported posts</h2>
    <table mat-table [dataSource]="reports()" class="mat-elevation-z1">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let r">{{ r.id }}</td>
      </ng-container>
      <ng-container matColumnDef="interview">
        <th mat-header-cell *matHeaderCellDef>Interview</th>
        <td mat-cell *matCellDef="let r"><a [routerLink]="['/interviews', r.interviewId]">#{{ r.interviewId }}</a></td>
      </ng-container>
      <ng-container matColumnDef="reason">
        <th mat-header-cell *matHeaderCellDef>Reason</th>
        <td mat-cell *matCellDef="let r">{{ r.reason }}</td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let r">{{ r.status }}</td>
      </ng-container>
      <ng-container matColumnDef="date">
        <th mat-header-cell *matHeaderCellDef>Reported</th>
        <td mat-cell *matCellDef="let r">{{ r.createdAt | date: 'short' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let r">
          <button mat-button (click)="resolve(r, 'REVIEWED')">Reviewed</button>
          <button mat-button (click)="resolve(r, 'DISMISSED')">Dismiss</button>
          <button mat-button color="warn" (click)="deletePost(r)">Delete post</button>
        </td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>
    @if (!reports().length) { <p class="empty">No reports.</p> }
  `,
  styles: [
    `
      .tools { display: flex; gap: 12px; align-items: baseline; flex-wrap: wrap; padding: 16px; margin-bottom: 16px; }
      table { width: 100%; }
      .empty { color: rgba(0, 0, 0, 0.6); margin-top: 12px; }
    `,
  ],
})
export class AdminDashboardComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  readonly reports = signal<Report[]>([]);
  readonly columns = ['id', 'interview', 'reason', 'status', 'date', 'actions'];
  statusFilter = '';
  banUserId?: number;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.adminService.listReports(this.statusFilter || undefined, 0, 100).subscribe((res) =>
      this.reports.set(res.content),
    );
  }

  resolve(report: Report, status: string): void {
    this.adminService.updateReport(report.id, status).subscribe(() => this.load());
  }

  deletePost(report: Report): void {
    if (!confirm(`Delete interview #${report.interviewId}?`)) {
      return;
    }
    this.adminService.deleteInterview(report.interviewId).subscribe(() => {
      this.snackBar.open('Post deleted', 'OK', { duration: 3000 });
      this.load();
    });
  }

  ban(): void {
    if (!this.banUserId) {
      return;
    }
    this.adminService.banUser(this.banUserId).subscribe(() =>
      this.snackBar.open(`User ${this.banUserId} banned`, 'OK', { duration: 3000 }),
    );
  }

  unban(): void {
    if (!this.banUserId) {
      return;
    }
    this.adminService.unbanUser(this.banUserId).subscribe(() =>
      this.snackBar.open(`User ${this.banUserId} unbanned`, 'OK', { duration: 3000 }),
    );
  }
}
