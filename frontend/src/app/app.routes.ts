import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';

/**
 * Every feature is lazy-loaded via {@code loadComponent}, so its JavaScript is only fetched when
 * the route is visited. This keeps the initial bundle small — important for fast first paint and
 * a key front-end scalability practice.
 */
export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'signup',
    loadComponent: () => import('./features/auth/signup.component').then((m) => m.SignupComponent),
  },
  {
    path: 'interviews',
    loadComponent: () =>
      import('./features/interview/interview-list.component').then((m) => m.InterviewListComponent),
  },
  {
    path: 'interviews/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/interview/interview-form.component').then((m) => m.InterviewFormComponent),
  },
  {
    path: 'interviews/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/interview/interview-form.component').then((m) => m.InterviewFormComponent),
  },
  {
    path: 'interviews/:id',
    loadComponent: () =>
      import('./features/interview/interview-detail.component').then(
        (m) => m.InterviewDetailComponent,
      ),
  },
  {
    path: 'my-interviews',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/interview/my-interviews.component').then((m) => m.MyInterviewsComponent),
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
