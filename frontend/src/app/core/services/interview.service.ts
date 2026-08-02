import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Comment,
  Interview,
  InterviewRequest,
  InterviewSummary,
  PagedResponse,
  SearchCriteria,
} from '../models/interview.model';

/** All read/write calls for interviews, comments, likes and reporting. */
@Injectable({ providedIn: 'root' })
export class InterviewService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/interviews`;

  search(criteria: SearchCriteria): Observable<PagedResponse<InterviewSummary>> {
    let params = new HttpParams();
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<PagedResponse<InterviewSummary>>(this.baseUrl, { params });
  }

  myInterviews(page = 0, size = 10, sort = 'newest'): Observable<PagedResponse<InterviewSummary>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<PagedResponse<InterviewSummary>>(`${this.baseUrl}/mine`, { params });
  }

  getById(id: number): Observable<Interview> {
    return this.http.get<Interview>(`${this.baseUrl}/${id}`);
  }

  create(request: InterviewRequest): Observable<Interview> {
    return this.http.post<Interview>(this.baseUrl, request);
  }

  update(id: number, request: InterviewRequest): Observable<Interview> {
    return this.http.put<Interview>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  like(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/like`, {});
  }

  unlike(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}/like`);
  }

  listComments(id: number, page = 0, size = 20): Observable<PagedResponse<Comment>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PagedResponse<Comment>>(`${this.baseUrl}/${id}/comments`, { params });
  }

  addComment(id: number, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.baseUrl}/${id}/comments`, { content });
  }

  deleteComment(interviewId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${interviewId}/comments/${commentId}`);
  }

  report(id: number, reason: string): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/${id}/report`, { reason });
  }
}
