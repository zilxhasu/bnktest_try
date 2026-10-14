package com.example.bnk.account.usecase;

import com.example.bnk.account.domain.AccountDomain;
import com.example.bnk.account.domain.BalanceDomain;
import com.example.bnk.account.infra.batch.BatchResultStore;
import com.example.bnk.account.infra.config.BnkProperties;
import com.example.bnk.account.infra.config.LoginInfoResolver;
import com.example.bnk.account.infra.req.BalanceQueryReq;
import com.example.bnk.account.infra.res.BalanceQueryRes;
import com.example.bnk.account.infra.corebank.CoreGateway;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import com.example.bnk.shared.login.LoginInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Function;

/**
 * 非同步聚合 – 第 1 步「prepare」：
 * 建立批次 → 每個帳號發一筆電文 → 立即回 batchId（不等主機），前端拿 batchId 去輪詢。
 * 好處：主機慢時 HTTP 執行緒不會被卡住，畫面也能逐筆顯示。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartBalanceQueryUseCase implements Function<ApiRequest<BalanceQueryReq>, ApiResponse<BalanceQueryRes>> {

    private final LoginInfoResolver loginInfoResolver;
    private final AccountDomain accountDomain;
    private final BalanceDomain balanceDomain;
    private final BatchResultStore batchResultStore;
    private final CoreGateway coreGateway;
    private final BnkProperties props;

    @Override
    public ApiResponse<BalanceQueryRes> apply(ApiRequest<BalanceQueryReq> req) {
        LoginInfo login = loginInfoResolver.current();
        var accountNos = req.getBody() == null ? null : req.getBody().getAccountNos();
        var accounts = accountDomain.requireOwned(login.companyId(), accountNos);

        String batchId = UUID.randomUUID().toString();
        var coreRequests = balanceDomain.buildTelegrams(batchId, accounts);
        // 一定要先建立批次再發電文，否則回覆可能比批次先到
        batchResultStore.create(batchId, login.companyId(), coreRequests.size());
        coreRequests.forEach(coreGateway::send);
        log.info("startQuery batchId={} coreRequests={}", batchId, coreRequests.size());

        return ApiResponse.ok(req.getHeader(),
                new BalanceQueryRes(batchId, coreRequests.size(), props.getBatch().getPollIntervalMs()));
    }
}
