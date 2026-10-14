package com.example.bnk.account.infra.res;

import java.util.List;

/**
 * @param done     全部電文都回來了嗎？false 時前端依 pollIntervalMs 繼續輪詢
 * @param items    已回來的帳號餘額（部分結果也先回，畫面可以逐步長出來）
 * @param totals   依幣別加總
 */
public record BalanceResultRes(boolean done, int total, int received, List<BalanceItem> items, List<CurrencyTotal> totals) {
}
