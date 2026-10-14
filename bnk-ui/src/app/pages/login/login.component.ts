import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <form class="card" (ngSubmit)="submit()">
      <h1>登入</h1>
      <label>使用者代號 <input name="userId" [(ngModel)]="userId" autocomplete="username" required /></label>
      <label>密碼 <input name="password" type="password" [(ngModel)]="password" autocomplete="current-password" required /></label>
      @if (error()) { <p class="error">{{ error() }}</p> }
      <button type="submit" [disabled]="loading()">{{ loading() ? '登入中…' : '登入' }}</button>
      <p class="hint">示範帳號：alice / demo1234（統編 12345678）</p>
    </form>
  `,
  styles: `
    .card { max-width: 360px; margin: 60px auto; background: #fff; padding: 32px; border-radius: 12px;
            display: flex; flex-direction: column; gap: 16px; box-shadow: 0 2px 12px rgba(0,0,0,.06); }
    h1 { margin: 0; font-size: 22px; }
    label { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: var(--muted); }
    .hint { font-size: 12px; color: var(--muted); margin: 0; }
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  userId = 'alice';
  password = '';
  readonly loading = signal(false);
  readonly error = signal('');

  submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.userId, this.password).subscribe({
      next: () => this.router.navigateByUrl('/overview'),
      error: () => {
        this.error.set('帳號或密碼錯誤');
        this.loading.set(false);
      },
    });
  }
}
