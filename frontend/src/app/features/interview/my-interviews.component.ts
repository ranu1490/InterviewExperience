import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { InterviewSummary } from '../../core/models/interview.model';
import { InterviewService } from '../../core/services/interview.service';

@Component({
  selector: 'app-my-interviews',
  imports: [RouterLink, DatePipe, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule],
  template: `
    <div class="head">
      <h1>My interview experiences</h1>
      <a mat-raised-button color="primary" routerLink="/interviews/new"><mat-icon>add</mat-icon> New</a>
    </div>

    <div class="grid">
      @for (item of items(); track item.id) {
        <mat-card class="card">
          <mat-card-header>
            <mat-card-title>{{ item.companyName }}</mat-card-title>
            <mat-card-subtitle>{{ item.jobRole }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <mat-chip>{{ item.selectionStatus }}</mat-chip>
            <div class="stats">
              <span><mat-icon>favorite</mat-icon>{{ item.totalLikes }}</span>
              <span><mat-icon>comment</mat-icon>{{ item.totalComments }}</span>
              <span><mat-icon>visibility</mat-icon>{{ item.views }}</span>
              <span class="date">{{ item.createdAt | date: 'mediumDate' }}</span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button [routerLink]="['/interviews', item.id]">View</a>
            <a mat-button [routerLink]="['/interviews', item.id, 'edit']">Edit</a>
          </mat-card-actions>
        </mat-card>
      } @empty {
        <p class="empty">You haven't shared any experiences yet.</p>
      }
    </div>
  `,
  styles: [
    `
      .head { display: flex; align-items: center; justify-content: space-between; }
      .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
      .stats { display: flex; gap: 14px; color: rgba(0, 0, 0, 0.6); font-size: 0.85rem; margin-top: 10px; }
      .stats span { display: inline-flex; align-items: center; gap: 3px; }
      .stats mat-icon { font-size: 16px; width: 16px; height: 16px; }
      .date { margin-left: auto; }
      .empty { color: rgba(0, 0, 0, 0.6); }
    `,
  ],
})
export class MyInterviewsComponent implements OnInit {
  private readonly interviewService = inject(InterviewService);
  readonly items = signal<InterviewSummary[]>([]);

  ngOnInit(): void {
    this.interviewService.myInterviews(0, 50).subscribe((res) => this.items.set(res.content));
  }
}
