import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

export interface ApiResponse<T> {
  returnCode: string;
  returnMsg: string;
  body: T;
}

/** 呼叫後端的唯一入口：包 {header, body} 信封、統一判斷 returnCode。 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  post<T>(svc: string, path: string, body: unknown = {}): Observable<T> {
    const header = { txnId: crypto.randomUUID(), channel: 'WEB', locale: 'zh-TW' };
    return this.http.post<ApiResponse<T>>(`/api/${svc}${path}`, { header, body }).pipe(
      map((res) => {
        if (res.returnCode !== '0000') {
          throw new Error(`${res.returnCode} ${res.returnMsg}`);
        }
        return res.body;
      }),
    );
  }
}
