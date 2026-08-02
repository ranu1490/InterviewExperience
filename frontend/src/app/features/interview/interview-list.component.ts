import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import {
  DIFFICULTY_LABELS,
  EXPERIENCE_LEVELS,
  InterviewSummary,
  SELECTION_STATUSES,
  SearchCriteria,
} from '../../core/models/interview.model';
import { InterviewService } from '../../core/services/interview.service';

/**
 * Browse/search page. Combines a debounced filter form, a sort selector and server-side pagination.
 * Filtering, sorting and paging all happen on the backend so the client only ever holds one page.
 */
@Component({
  selector: 'app-interview-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatPaginatorModule,
  ],
  template: `
    <h1>Interview experiences</h1>

    <form [formGroup]="filters" class="filters">
      <mat-form-field appearance="outline" class="grow">
        <mat-label>Search company, role or text</mat-label>
        <input matInput formControlName="keyword" />
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Experience level</mat-label>
        <mat-select formControlName="experienceLevel">
          <mat-option [value]="''">Any</mat-option>
          @for (lvl of levels; track lvl) { <mat-option [value]="lvl">{{ lvl }}</mat-option> }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select formControlName="selectionStatus">
          <mat-option [value]="''">Any</mat-option>
          @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Difficulty</mat-label>
        <mat-select formControlName="difficultyLabel">
          <mat-option [value]="''">Any</mat-option>
          @for (d of difficulties; track d) { <mat-option [value]="d">{{ d }}</mat-option> }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Location</mat-label>
        <input matInput formControlName="location" />
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Tag</mat-label>
        <input matInput formControlName="tag" />
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Sort by</mat-label>
        <mat-select formControlName="sort">
          <mat-option value="newest">Newest</mat-option>
          <mat-option value="oldest">Oldest</mat-option>
          <mat-option value="mostViewed">Most viewed</mat-option>
          <mat-option value="mostHelpful">Most helpful</mat-option>
          <mat-option value="highestDifficulty">Highest difficulty</mat-option>
        </mat-select>
      </mat-form-field>
    </form>

    <div class="grid">
      @for (item of results(); track item.id) {
        <mat-card class="card" [routerLink]="['/interviews', item.id]">
          <mat-card-header>
            <mat-card-title>{{ item.companyName }}</mat-card-title>
            <mat-card-subtitle>{{ item.jobRole }} · {{ item.experienceLevel }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <div class="meta">
              @if (item.difficultyLabel) {
                <mat-chip [class]="'diff-' + item.difficultyLabel.toLowerCase()">
                  {{ item.difficultyLabel }} · {{ item.difficultyScore }}/10
                </mat-chip>
              }
              <span class="status">{{ item.selectionStatus }}</span>
              @if (item.location) { <span class="loc">{{ item.location }}</span> }
            </div>
            @if (item.tags.length) {
              <mat-chip-set>
                @for (tag of item.tags; track tag) { <mat-chip>{{ tag }}</mat-chip> }
              </mat-chip-set>
            }
            <div class="stats">
              <span><mat-icon>favorite</mat-icon>{{ item.totalLikes }}</span>
              <span><mat-icon>comment</mat-icon>{{ item.totalComments }}</span>
              <span><mat-icon>visibility</mat-icon>{{ item.views }}</span>
              <span class="by">by {{ item.authorUsername }} · {{ item.createdAt | date: 'mediumDate' }}</span>
            </div>
          </mat-card-content>
        </mat-card>
      } @empty {
        <p class="empty">No experiences match your filters.</p>
      }
    </div>

    <mat-paginator
      [length]="total()"
      [pageSize]="size"
      [pageIndex]="page()"
      [pageSizeOptions]="[6, 10, 20, 50]"
      (page)="onPage($event)"
    />
  `,
  styles: [
    `
      .filters {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin-bottom: 16px;
      }
      .filters .grow { flex: 1 1 260px; }
      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 16px;
      }
      .card { cursor: pointer; }
      .card:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15); }
      .meta { display: flex; align-items: center; gap: 10px; margin: 8px 0; flex-wrap: wrap; }
      .status, .loc { font-size: 0.8rem; color: rgba(0, 0, 0, 0.6); }
      .stats {
        display: flex; align-items: center; gap: 14px;
        color: rgba(0, 0, 0, 0.6); font-size: 0.82rem; margin-top: 8px; flex-wrap: wrap;
      }
      .stats span { display: inline-flex; align-items: center; gap: 3px; }
      .stats mat-icon { font-size: 16px; width: 16px; height: 16px; }
      .by { margin-left: auto; }
      .diff-easy { background: #c8e6c9; }
      .diff-medium { background: #fff0c2; }
      .diff-hard { background: #ffcdd2; }
      .empty { color: rgba(0, 0, 0, 0.6); }
      mat-paginator { margin-top: 16px; background: transparent; }
    `,
  ],
})
export class InterviewListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly interviewService = inject(InterviewService);

  readonly levels = EXPERIENCE_LEVELS;
  readonly statuses = SELECTION_STATUSES;
  readonly difficulties = DIFFICULTY_LABELS;

  readonly results = signal<InterviewSummary[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  size = 10;

  readonly filters = this.fb.nonNullable.group({
    keyword: [''],
    experienceLevel: [''],
    selectionStatus: [''],
    difficultyLabel: [''],
    location: [''],
    tag: [''],
    sort: ['newest'],
  });

  ngOnInit(): void {
    this.filters.valueChanges.pipe(debounceTime(350)).subscribe(() => {
      this.page.set(0);
      this.load();
    });
    this.load();
  }

  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size = event.pageSize;
    this.load();
  }

  private load(): void {
    const f = this.filters.getRawValue();
    const criteria: SearchCriteria = {
      keyword: f.keyword || undefined,
      experienceLevel: (f.experienceLevel as SearchCriteria['experienceLevel']) || undefined,
      selectionStatus: (f.selectionStatus as SearchCriteria['selectionStatus']) || undefined,
      difficultyLabel: (f.difficultyLabel as SearchCriteria['difficultyLabel']) || undefined,
      location: f.location || undefined,
      tag: f.tag || undefined,
      sort: f.sort,
      page: this.page(),
      size: this.size,
    };
    this.interviewService.search(criteria).subscribe((res) => {
      this.results.set(res.content);
      this.total.set(res.totalElements);
    });
  }
}
