package com.example.bnk.account.infra.res;

/**
 * @param batchId            之後用來輪詢 getResult
 * @param total            共發出幾筆電文
 * @param pollIntervalMs 建議前端幾毫秒後再來問
 */
public record BalanceQueryRes(String batchId, int total, int pollIntervalMs) {
}
