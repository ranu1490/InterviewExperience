import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { InterviewSummary } from '../../core/models/interview.model';
import { AuthService } from '../../core/services/auth.service';
import { InterviewService } from '../../core/services/interview.service';

/** Marketing landing page with a hero and the latest interview experiences. */
@Component({
  selector: 'app-landing',
  imports: [
    RouterLink,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
  ],
  template: `
    <section class="hero">
      <h1>Real interview experiences, shared by real candidates</h1>
      <p>
        Read, search and learn from thousands of interview experiences across companies and roles —
        with AI-graded difficulty and suggested topics. No login needed to read.
      </p>
      <div class="hero-actions">
        <a mat-raised-button color="primary" routerLink="/interviews">Browse experiences</a>
        @if (!auth.isAuthenticated()) {
          <a mat-stroked-button routerLink="/signup">Share yours</a>
        }
      </div>
    </section>

    <h2>Latest experiences</h2>
    <div class="grid">
      @for (item of latest(); track item.id) {
        <mat-card class="card" [routerLink]="['/interviews', item.id]">
          <mat-card-header>
            <mat-card-title>{{ item.companyName }}</mat-card-title>
            <mat-card-subtitle>{{ item.jobRole }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <div class="meta">
              @if (item.difficultyLabel) {
                <mat-chip [class]="'diff-' + item.difficultyLabel.toLowerCase()">
                  {{ item.difficultyLabel }} · {{ item.difficultyScore }}/10
                </mat-chip>
              }
              <span class="status">{{ item.selectionStatus }}</span>
            </div>
            <div class="stats">
              <span><mat-icon>favorite</mat-icon>{{ item.totalLikes }}</span>
              <span><mat-icon>comment</mat-icon>{{ item.totalComments }}</span>
              <span><mat-icon>visibility</mat-icon>{{ item.views }}</span>
              <span class="date">{{ item.createdAt | date: 'mediumDate' }}</span>
            </div>
          </mat-card-content>
        </mat-card>
      } @empty {
        <p class="empty">No experiences yet. Be the first to share!</p>
      }
    </div>
  `,
  styles: [
    `
      .hero {
        text-align: center;
        padding: 64px 24px 56px;
        margin: -8px 0 32px;
        border-radius: 20px;
        color: #fff;
        background: linear-gradient(135deg, #0d47a1 0%, #1565c0 45%, #1e88e5 100%);
        box-shadow: 0 18px 44px rgba(21, 101, 192, 0.35);
        position: relative;
        overflow: hidden;
      }
      /* Subtle decorative glow circles. */
      .hero::before,
      .hero::after {
        content: '';
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.12);
      }
      .hero::before { width: 260px; height: 260px; top: -90px; right: -60px; }
      .hero::after { width: 180px; height: 180px; bottom: -70px; left: -40px; }
      .hero h1 {
        font-size: 2.4rem;
        margin-bottom: 14px;
        color: #fff;
        position: relative;
      }
      .hero p {
        max-width: 660px;
        margin: 0 auto 26px;
        color: rgba(255, 255, 255, 0.9);
        font-size: 1.05rem;
        position: relative;
      }
      .hero-actions {
        display: flex;
        gap: 12px;
        justify-content: center;
        position: relative;
      }
      .hero-actions a[mat-stroked-button] {
        color: #fff;
        border-color: rgba(255, 255, 255, 0.7);
      }
      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
      }
      .card {
        cursor: pointer;
        transition: box-shadow 0.15s ease;
      }
      .card:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      }
      .meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 8px 0;
      }
      .status {
        font-size: 0.8rem;
        color: rgba(0, 0, 0, 0.6);
      }
      .stats {
        display: flex;
        align-items: center;
        gap: 14px;
        color: rgba(0, 0, 0, 0.6);
        font-size: 0.85rem;
      }
      .stats span {
        display: inline-flex;
        align-items: center;
        gap: 3px;
      }
      .stats mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }
      .date {
        margin-left: auto;
      }
      .diff-easy { background: #c8e6c9; }
      .diff-medium { background: #fff0c2; }
      .diff-hard { background: #ffcdd2; }
      .empty { color: rgba(0, 0, 0, 0.6); }
    `,
  ],
})
export class LandingComponent implements OnInit {
  private readonly interviewService = inject(InterviewService);
  readonly auth = inject(AuthService);
  readonly latest = signal<InterviewSummary[]>([]);

  ngOnInit(): void {
    this.interviewService.search({ page: 0, size: 6, sort: 'newest' }).subscribe((res) => {
      this.latest.set(res.content);
    });
  }
}
