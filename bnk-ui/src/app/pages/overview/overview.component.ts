import { DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { switchMap, takeWhile, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';

interface Account { accountNo: string; area: string; currency: string; accountType: string; alias: string | null; }
interface BalanceItem { accountNo: string; area: string; currency: string; alias: string | null; balance: number | null; status: string; }
interface CurrencyTotal { currency: string; total: number; count: number; }
interface Summary { done: boolean; total: number; received: number; items: BalanceItem[]; totals: CurrencyTotal[]; }
interface Prepare { batchId: string; total: number; pollIntervalMs: number; }

/**
 * 帳戶總覽：
 * 1. listAccounts 取帳號清單（DB）
 * 2. startQuery 發電文 → 拿 batchId
 * 3. 每 pollIntervalMs 輪詢 getResult，直到 done，畫面逐筆長出餘額
 */
@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [DecimalPipe, FormsModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.css',
})
export class OverviewComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly accounts = signal<Account[]>([]);
  readonly summary = signal<Summary | null>(null);
  readonly querying = signal(false);
  readonly error = signal('');
  editing: string | null = null;
  aliasDraft = '';

  ngOnInit(): void {
    this.loadAccounts();
    this.queryBalance();
  }

  loadAccounts(): void {
    this.api.post<{ accounts: Account[] }>('account', '/accounts/list').subscribe({
      next: (b) => this.accounts.set(b.accounts),
      error: (e: Error) => this.error.set(e.message),
    });
  }

  queryBalance(): void {
    this.querying.set(true);
    this.summary.set(null);
    this.api
      .post<Prepare>('account', '/balances/query', { accountNos: [] })
      .pipe(
        switchMap((p) =>
          timer(0, p.pollIntervalMs).pipe(
            switchMap(() => this.api.post<Summary>('account', '/balances/result', { batchId: p.batchId })),
            takeWhile((s) => !s.done, true),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (s) => this.summary.set(s),
        error: (e: Error) => { this.error.set(e.message); this.querying.set(false); },
        complete: () => this.querying.set(false),
      });
  }

  balanceOf(accountNo: string): BalanceItem | undefined {
    return this.summary()?.items.find((i) => i.accountNo === accountNo);
  }

  /** 畫面上帳號遮罩：前 3 後 3 */
  mask(account: string): string {
    return account.length <= 6 ? account : account.slice(0, 3) + '*'.repeat(account.length - 6) + account.slice(-3);
  }

  startEdit(a: Account): void {
    this.editing = a.accountNo;
    this.aliasDraft = a.alias ?? '';
  }

  saveAlias(accountNo: string): void {
    this.api.post('account', '/accounts/alias', { accountNo, alias: this.aliasDraft }).subscribe({
      next: () => { this.editing = null; this.loadAccounts(); },
      error: (e: Error) => this.error.set(e.message),
    });
  }
}
