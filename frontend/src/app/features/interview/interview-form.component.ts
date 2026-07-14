import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import {
  EXPERIENCE_LEVELS,
  InterviewRequest,
  QUESTION_CATEGORIES,
  SELECTION_STATUSES,
} from '../../core/models/interview.model';
import { InterviewService } from '../../core/services/interview.service';

/**
 * Create/edit form. Uses reactive FormArrays for the variable-length rounds and questions lists,
 * which is exactly what FormArray is designed for — add/remove rows bind directly to the model.
 */
@Component({
  selector: 'app-interview-form',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h1>{{ editId ? 'Edit' : 'Share' }} interview experience</h1>

    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-card class="section">
        <mat-card-content class="row">
          <mat-form-field appearance="outline" class="grow">
            <mat-label>Company name</mat-label>
            <input matInput formControlName="companyName" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="grow">
            <mat-label>Job role</mat-label>
            <input matInput formControlName="jobRole" />
          </mat-form-field>
        </mat-card-content>
        <mat-card-content class="row">
          <mat-form-field appearance="outline">
            <mat-label>Experience level</mat-label>
            <mat-select formControlName="experienceLevel">
              @for (lvl of levels; track lvl) { <mat-option [value]="lvl">{{ lvl }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Years of experience</mat-label>
            <input matInput type="number" formControlName="yearsOfExperience" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Selection status</mat-label>
            <mat-select formControlName="selectionStatus">
              @for (s of statuses; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
            </mat-select>
          </mat-form-field>
        </mat-card-content>
        <mat-card-content class="row">
          <mat-form-field appearance="outline">
            <mat-label>Interview date</mat-label>
            <input matInput type="date" formControlName="interviewDate" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Location</mat-label>
            <input matInput formControlName="location" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>CTC offered</mat-label>
            <input matInput formControlName="ctcOffered" />
          </mat-form-field>
        </mat-card-content>
      </mat-card>

      <mat-card class="section">
        <div class="section-head"><h2>Rounds</h2><button mat-stroked-button type="button" (click)="addRound()"><mat-icon>add</mat-icon> Add round</button></div>
        <div formArrayName="rounds">
          @for (round of rounds.controls; track $index; let i = $index) {
            <div class="row" [formGroupName]="i">
              <mat-form-field appearance="outline" class="small">
                <mat-label>#</mat-label>
                <input matInput type="number" formControlName="roundNumber" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Name</mat-label>
                <input matInput formControlName="name" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="grow">
                <mat-label>Description</mat-label>
                <input matInput formControlName="description" />
              </mat-form-field>
              <button mat-icon-button type="button" (click)="rounds.removeAt(i)"><mat-icon>delete</mat-icon></button>
            </div>
          }
        </div>
      </mat-card>

      <mat-card class="section">
        <div class="section-head"><h2>Questions</h2><button mat-stroked-button type="button" (click)="addQuestion()"><mat-icon>add</mat-icon> Add question</button></div>
        <div formArrayName="questions">
          @for (q of questions.controls; track $index; let i = $index) {
            <div class="row" [formGroupName]="i">
              <mat-form-field appearance="outline">
                <mat-label>Category</mat-label>
                <mat-select formControlName="category">
                  @for (c of categories; track c) { <mat-option [value]="c">{{ c }}</mat-option> }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" class="grow">
                <mat-label>Question</mat-label>
                <input matInput formControlName="question" />
              </mat-form-field>
              <button mat-icon-button type="button" (click)="questions.removeAt(i)"><mat-icon>delete</mat-icon></button>
            </div>
          }
        </div>
      </mat-card>

      <mat-card class="section">
        <mat-card-content>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Overall experience</mat-label>
            <textarea matInput rows="4" formControlName="overallExperience"></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Preparation tips</mat-label>
            <textarea matInput rows="3" formControlName="preparationTips"></textarea>
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Tags (comma separated)</mat-label>
            <input matInput formControlName="tagsCsv" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Resources used (comma separated)</mat-label>
            <input matInput formControlName="resourcesCsv" />
          </mat-form-field>
        </mat-card-content>
      </mat-card>

      <div class="submit">
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || saving()">
          {{ editId ? 'Save changes' : 'Publish' }}
        </button>
      </div>
    </form>
  `,
  styles: [
    `
      .section { margin-bottom: 16px; }
      .row { display: flex; gap: 12px; align-items: baseline; flex-wrap: wrap; }
      .grow { flex: 1 1 200px; }
      .small { width: 90px; }
      .full { width: 100%; }
      .section-head { display: flex; align-items: center; justify-content: space-between; padding: 0 16px; }
      .submit { margin: 24px 0 48px; }
    `,
  ],
})
export class InterviewFormComponent implements OnInit {
  @Input() id?: string;

  private readonly fb = inject(FormBuilder);
  private readonly interviewService = inject(InterviewService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly levels = EXPERIENCE_LEVELS;
  readonly statuses = SELECTION_STATUSES;
  readonly categories = QUESTION_CATEGORIES;
  readonly saving = signal(false);
  editId?: number;

  readonly form = this.fb.nonNullable.group({
    companyName: ['', Validators.required],
    jobRole: ['', Validators.required],
    experienceLevel: ['MID', Validators.required],
    yearsOfExperience: [null as number | null],
    interviewDate: [''],
    location: [''],
    ctcOffered: [''],
    selectionStatus: ['SELECTED', Validators.required],
    overallExperience: [''],
    preparationTips: [''],
    tagsCsv: [''],
    resourcesCsv: [''],
    rounds: this.fb.array<ReturnType<InterviewFormComponent['newRound']>>([]),
    questions: this.fb.array<ReturnType<InterviewFormComponent['newQuestion']>>([]),
  });

  get rounds(): FormArray {
    return this.form.get('rounds') as FormArray;
  }

  get questions(): FormArray {
    return this.form.get('questions') as FormArray;
  }

  ngOnInit(): void {
    if (this.id) {
      this.editId = Number(this.id);
      this.interviewService.getById(this.editId).subscribe((iv) => {
        this.form.patchValue({
          companyName: iv.companyName,
          jobRole: iv.jobRole,
          experienceLevel: iv.experienceLevel,
          yearsOfExperience: iv.yearsOfExperience ?? null,
          interviewDate: iv.interviewDate ?? '',
          location: iv.location ?? '',
          ctcOffered: iv.ctcOffered ?? '',
          selectionStatus: iv.selectionStatus,
          overallExperience: iv.overallExperience ?? '',
          preparationTips: iv.preparationTips ?? '',
          tagsCsv: iv.tags.join(', '),
          resourcesCsv: iv.resourcesUsed.join(', '),
        });
        iv.rounds.forEach((r) => this.rounds.push(this.newRound(r.roundNumber, r.name, r.description)));
        iv.questions.forEach((q) => this.questions.push(this.newQuestion(q.category, q.question)));
      });
    } else {
      this.addRound();
      this.addQuestion();
    }
  }

  newRound(roundNumber = 1, name = '', description = '') {
    return this.fb.nonNullable.group({
      roundNumber: [roundNumber, Validators.required],
      name: [name, Validators.required],
      description: [description],
    });
  }

  newQuestion(category = 'DSA', question = '') {
    return this.fb.nonNullable.group({
      category: [category, Validators.required],
      question: [question, Validators.required],
    });
  }

  addRound(): void {
    this.rounds.push(this.newRound(this.rounds.length + 1));
  }

  addQuestion(): void {
    this.questions.push(this.newQuestion());
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const request: InterviewRequest = {
      companyName: v.companyName,
      jobRole: v.jobRole,
      experienceLevel: v.experienceLevel as InterviewRequest['experienceLevel'],
      yearsOfExperience: v.yearsOfExperience ?? undefined,
      interviewDate: v.interviewDate || undefined,
      location: v.location || undefined,
      ctcOffered: v.ctcOffered || undefined,
      selectionStatus: v.selectionStatus as InterviewRequest['selectionStatus'],
      numberOfRounds: v.rounds.length,
      rounds: v.rounds as InterviewRequest['rounds'],
      questions: v.questions as InterviewRequest['questions'],
      overallExperience: v.overallExperience || undefined,
      preparationTips: v.preparationTips || undefined,
      tags: this.csv(v.tagsCsv),
      resourcesUsed: this.csv(v.resourcesCsv),
    };

    this.saving.set(true);
    const call = this.editId
      ? this.interviewService.update(this.editId, request)
      : this.interviewService.create(request);
    call.subscribe({
      next: (iv) => {
        this.snackBar.open('Saved', 'OK', { duration: 3000 });
        this.router.navigate(['/interviews', iv.id]);
      },
      error: () => this.saving.set(false),
    });
  }

  private csv(value: string): string[] {
    return value
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
  }
}
