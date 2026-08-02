import { DatePipe } from '@angular/common';
import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { Comment, Interview } from '../../core/models/interview.model';
import { AuthService } from '../../core/services/auth.service';
import { InterviewService } from '../../core/services/interview.service';

@Component({
  selector: 'app-interview-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    @if (interview(); as iv) {
      <div class="header">
        <div>
          <h1>{{ iv.companyName }}</h1>
          <p class="sub">{{ iv.jobRole }} · {{ iv.experienceLevel }} · {{ iv.location }}</p>
        </div>
        @if (canManage(iv)) {
          <div class="owner-actions">
            @if (isOwner(iv)) {
              <a mat-stroked-button [routerLink]="['/interviews', iv.id, 'edit']">
                <mat-icon>edit</mat-icon> Edit
              </a>
            }
            <button mat-stroked-button color="warn" (click)="remove(iv)">
              <mat-icon>delete</mat-icon> Delete
            </button>
          </div>
        }
      </div>

      <div class="badges">
        @if (iv.difficultyLabel) {
          <mat-chip [class]="'diff-' + iv.difficultyLabel.toLowerCase()">
            {{ iv.difficultyLabel }} · {{ iv.difficultyScore }}/10
          </mat-chip>
        }
        <mat-chip>{{ iv.selectionStatus }}</mat-chip>
        @if (iv.ctcOffered) { <mat-chip>CTC: {{ iv.ctcOffered }}</mat-chip> }
        @if (iv.yearsOfExperience != null) { <mat-chip>{{ iv.yearsOfExperience }} yrs exp</mat-chip> }
      </div>

      <div class="actions">
        <button mat-raised-button [color]="iv.likedByCurrentUser ? 'accent' : 'primary'" (click)="toggleLike(iv)">
          <mat-icon>{{ iv.likedByCurrentUser ? 'favorite' : 'favorite_border' }}</mat-icon>
          {{ iv.likedByCurrentUser ? 'Liked' : 'Like' }} ({{ iv.totalLikes }})
        </button>
        <button mat-stroked-button (click)="report(iv)"><mat-icon>flag</mat-icon> Report</button>
        <span class="views"><mat-icon>visibility</mat-icon>{{ iv.views }} views</span>
      </div>

      @if (iv.aiSummary) {
        <mat-card class="ai">
          <mat-card-header>
            <mat-card-title><mat-icon>auto_awesome</mat-icon> AI summary</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>{{ iv.aiSummary }}</p>
            @if (iv.aiSuggestedTopics.length) {
              <mat-chip-set>
                @for (t of iv.aiSuggestedTopics; track t) { <mat-chip>{{ t }}</mat-chip> }
              </mat-chip-set>
            }
          </mat-card-content>
        </mat-card>
      }

      @if (iv.rounds.length) {
        <h2>Rounds ({{ iv.numberOfRounds }})</h2>
        @for (round of iv.rounds; track round.roundNumber) {
          <mat-card class="section">
            <mat-card-header><mat-card-title>Round {{ round.roundNumber }}: {{ round.name }}</mat-card-title></mat-card-header>
            <mat-card-content>{{ round.description }}</mat-card-content>
          </mat-card>
        }
      }

      @if (iv.questions.length) {
        <h2>Questions</h2>
        <mat-card class="section">
          <mat-card-content>
            @for (q of iv.questions; track $index) {
              <div class="q"><mat-chip>{{ q.category }}</mat-chip> <span>{{ q.question }}</span></div>
            }
          </mat-card-content>
        </mat-card>
      }

      @if (iv.overallExperience) {
        <h2>Overall experience</h2>
        <mat-card class="section"><mat-card-content>{{ iv.overallExperience }}</mat-card-content></mat-card>
      }
      @if (iv.preparationTips) {
        <h2>Preparation tips</h2>
        <mat-card class="section"><mat-card-content>{{ iv.preparationTips }}</mat-card-content></mat-card>
      }
      @if (iv.resourcesUsed.length) {
        <h2>Resources</h2>
        <ul>@for (r of iv.resourcesUsed; track r) { <li>{{ r }}</li> }</ul>
      }

      <mat-divider />
      <h2>Comments ({{ iv.totalComments }})</h2>

      @if (auth.isAuthenticated()) {
        <form [formGroup]="commentForm" (ngSubmit)="addComment(iv)" class="comment-form">
          <mat-form-field appearance="outline" class="grow">
            <mat-label>Add a comment</mat-label>
            <input matInput formControlName="content" />
          </mat-form-field>
          <button mat-raised-button color="primary" [disabled]="commentForm.invalid">Post</button>
        </form>
      } @else {
        <p><a routerLink="/login">Log in</a> to comment.</p>
      }

      @for (c of comments(); track c.id) {
        <mat-card class="comment">
          <div class="comment-head">
            <strong>{{ c.username }}</strong>
            <span class="time">{{ c.createdAt | date: 'short' }}</span>
            @if (canDeleteComment(c)) {
              <button mat-icon-button (click)="deleteComment(iv, c)"><mat-icon>delete</mat-icon></button>
            }
          </div>
          <p>{{ c.content }}</p>
        </mat-card>
      } @empty {
        <p class="empty">No comments yet.</p>
      }
    }
  `,
  styles: [
    `
      .header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
      .header h1 { margin-bottom: 4px; }
      .sub { color: rgba(0, 0, 0, 0.6); }
      .owner-actions { display: flex; gap: 8px; }
      .badges, .actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin: 12px 0; }
      .views { display: inline-flex; align-items: center; gap: 4px; color: rgba(0, 0, 0, 0.6); }
      .ai {
        margin: 16px 0;
        background: linear-gradient(135deg, #e8f1ff 0%, #f4f8ff 100%) !important;
        border: 1px solid rgba(21, 101, 192, 0.18) !important;
      }
      .ai mat-card-title { display: flex; align-items: center; gap: 6px; color: #0d47a1; }
      .ai mat-icon { color: #1976d2; }
      .section { margin-bottom: 12px; }
      .q { display: flex; align-items: center; gap: 10px; padding: 6px 0; }
      .comment-form { display: flex; gap: 12px; align-items: baseline; margin: 12px 0; }
      .comment-form .grow { flex: 1; }
      .comment { margin-bottom: 10px; padding: 10px 14px; }
      .comment-head { display: flex; align-items: center; gap: 10px; }
      .comment-head .time { color: rgba(0, 0, 0, 0.5); font-size: 0.8rem; }
      .comment-head button { margin-left: auto; }
      .diff-easy { background: #c8e6c9; }
      .diff-medium { background: #fff0c2; }
      .diff-hard { background: #ffcdd2; }
      .empty { color: rgba(0, 0, 0, 0.6); }
    `,
  ],
})
export class InterviewDetailComponent implements OnInit {
  @Input() id!: string;

  private readonly interviewService = inject(InterviewService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);

  readonly interview = signal<Interview | null>(null);
  readonly comments = signal<Comment[]>([]);

  readonly commentForm = this.fb.nonNullable.group({
    content: ['', Validators.required],
  });

  ngOnInit(): void {
    const id = Number(this.id);
    this.interviewService.getById(id).subscribe((iv) => this.interview.set(iv));
    this.interviewService.listComments(id).subscribe((res) => this.comments.set(res.content));
  }

  isOwner(iv: Interview): boolean {
    return this.auth.user()?.id === iv.authorId;
  }

  canManage(iv: Interview): boolean {
    return this.isOwner(iv) || this.auth.isAdmin();
  }

  canDeleteComment(c: Comment): boolean {
    return this.auth.user()?.id === c.userId || this.auth.isAdmin();
  }

  toggleLike(iv: Interview): void {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: `/interviews/${iv.id}` } });
      return;
    }
    const request = iv.likedByCurrentUser
      ? this.interviewService.unlike(iv.id)
      : this.interviewService.like(iv.id);
    request.subscribe(() => {
      this.interview.update((cur) =>
        cur
          ? {
              ...cur,
              likedByCurrentUser: !cur.likedByCurrentUser,
              totalLikes: cur.totalLikes + (cur.likedByCurrentUser ? -1 : 1),
            }
          : cur,
      );
    });
  }

  addComment(iv: Interview): void {
    if (this.commentForm.invalid) {
      return;
    }
    this.interviewService.addComment(iv.id, this.commentForm.getRawValue().content).subscribe((c) => {
      this.comments.update((list) => [c, ...list]);
      this.interview.update((cur) => (cur ? { ...cur, totalComments: cur.totalComments + 1 } : cur));
      this.commentForm.reset();
    });
  }

  deleteComment(iv: Interview, c: Comment): void {
    this.interviewService.deleteComment(iv.id, c.id).subscribe(() => {
      this.comments.update((list) => list.filter((x) => x.id !== c.id));
      this.interview.update((cur) => (cur ? { ...cur, totalComments: cur.totalComments - 1 } : cur));
    });
  }

  report(iv: Interview): void {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    const reason = prompt('Why are you reporting this post?');
    if (reason) {
      this.interviewService.report(iv.id, reason).subscribe(() =>
        this.snackBar.open('Report submitted for review. Thank you.', 'OK', { duration: 4000 }),
      );
    }
  }

  remove(iv: Interview): void {
    if (!confirm('Delete this interview experience?')) {
      return;
    }
    this.interviewService.delete(iv.id).subscribe(() => {
      this.snackBar.open('Deleted', 'OK', { duration: 3000 });
      this.router.navigate(['/interviews']);
    });
  }
}
