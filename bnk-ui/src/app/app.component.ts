import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header class="top">
      <strong>BNK 企業網銀</strong><span class="tag">DEMO</span>
      @if (auth.user(); as u) {
        <span class="spacer"></span>
        <span>{{ u }}</span>
        <button class="ghost" (click)="auth.logout()">登出</button>
      }
    </header>
    <main><router-outlet /></main>
  `,
  styles: `
    .top { display: flex; align-items: center; gap: 12px; padding: 12px 24px; background: var(--brand); color: #fff; }
    .top .ghost { color: #fff; border-color: #fff; }
    .tag { font-size: 11px; background: rgba(255,255,255,.2); padding: 2px 6px; border-radius: 4px; }
    .spacer { flex: 1; }
    main { max-width: 1040px; margin: 24px auto; padding: 0 16px; }
  `,
})
export class AppComponent {
  readonly auth = inject(AuthService);
}
