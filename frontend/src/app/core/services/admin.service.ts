import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse, Report } from '../models/interview.model';
import { User } from '../models/user.model';

/** Admin-only moderation calls (report queue, delete any post, ban users). */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly adminUrl = `${environment.apiUrl}/admin`;
  private readonly usersUrl = `${environment.apiUrl}/users`;

  listReports(status?: string, page = 0, size = 20): Observable<PagedResponse<Report>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PagedResponse<Report>>(`${this.adminUrl}/reports`, { params });
  }

  updateReport(reportId: number, status: string): Observable<Report> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Report>(`${this.adminUrl}/reports/${reportId}`, {}, { params });
  }

  deleteInterview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/interviews/${id}`);
  }

  banUser(userId: number): Observable<User> {
    return this.http.post<User>(`${this.usersUrl}/${userId}/ban`, {});
  }

  unbanUser(userId: number): Observable<User> {
    return this.http.post<User>(`${this.usersUrl}/${userId}/unban`, {});
  }
}
