import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

interface LoginRes {
  returnCode: string;
  token: string;
  expiresIn: number;
  userId: string;
}

/** 登入狀態。token 放 sessionStorage：關掉分頁就失效，比 localStorage 安全一些。 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly user = signal<string | null>(sessionStorage.getItem('bnk.user'));

  get token(): string | null {
    return sessionStorage.getItem('bnk.token');
  }

  login(userId: string, password: string): Observable<LoginRes> {
    return this.http.post<LoginRes>('/auth/login', { userId, password }).pipe(
      tap((res) => {
        sessionStorage.setItem('bnk.token', res.token);
        sessionStorage.setItem('bnk.user', res.userId);
        this.user.set(res.userId);
      }),
    );
  }

  logout(): void {
    sessionStorage.removeItem('bnk.token');
    sessionStorage.removeItem('bnk.user');
    this.user.set(null);
    this.router.navigateByUrl('/login');
  }
}
