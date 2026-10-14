// k6 壓力測試（免費開源）：k6 run -e BASE=http://localhost:4200 -e VUS=10 -e DUR=1m k6/smoke.js
// 情境：登入 → 帳號清單 → 發起餘額查詢 → 輪詢到完成
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8080';
export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: __ENV.DUR || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],      // 錯誤率 < 1%
    http_req_duration: ['p(95)<500'],    // 95% 請求 < 500ms
  },
};

const json = { headers: { 'Content-Type': 'application/json' } };

export function setup() {
  const res = http.post(`${BASE}/auth/login`, JSON.stringify({ userId: 'alice', password: __ENV.DEMO_PASSWORD || 'demo1234' }), json);
  check(res, { 'login 200': (r) => r.status === 200 });
  return { token: res.json('token') };
}

export default function ({ token }) {
  const p = { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } };
  const call = (path, body) => http.post(`${BASE}/api/account${path}`, JSON.stringify({ header: {}, body }), p);

  const list = call('/accounts/list', {});
  check(list, { 'list ok': (r) => r.json('returnCode') === '0000' });

  const prep = call('/balances/query', { accountNos: [] });
  const batchId = prep.json('body.batchId');
  for (let i = 0; i < 20; i++) {
    const s = call('/balances/result', { batchId });
    if (s.json('body.done')) {
      check(s, { 'summary done': () => true });
      break;
    }
    sleep(0.5);
  }
  sleep(1);
}
