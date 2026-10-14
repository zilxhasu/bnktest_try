package com.example.bnk.account.infra.batch;

import com.example.bnk.shared.corebank.CoreResponse;

import java.util.List;

/**
 * 聚合批次目前狀態。
 *
 * @param ownerCompanyId 建立批次的統編（取回時要驗證，防止越權讀別人的批次）
 * @param total     預計回覆的電文數
 * @param results   已收到的回覆
 */
public record BatchSnapshot(String ownerCompanyId, int total, List<CoreResponse> results) {

    public boolean done() {
        return results.size() >= total;
    }
}
