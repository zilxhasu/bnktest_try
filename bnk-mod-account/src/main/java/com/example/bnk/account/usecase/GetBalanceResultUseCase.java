package com.example.bnk.account.usecase;

import com.example.bnk.account.domain.AccountDomain;
import com.example.bnk.account.domain.BalanceDomain;
import com.example.bnk.account.infra.batch.BatchResultStore;
import com.example.bnk.account.infra.config.LoginInfoResolver;
import com.example.bnk.account.infra.req.BalanceResultReq;
import com.example.bnk.account.infra.res.BalanceResultRes;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import com.example.bnk.shared.api.BizException;
import com.example.bnk.shared.api.ReturnCode;
import com.example.bnk.shared.login.LoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/** 非同步聚合 – 第 2 步「get」：依 batchId 取回目前已收到的結果，done=false 就繼續輪詢。 */
@Service
@RequiredArgsConstructor
public class GetBalanceResultUseCase implements Function<ApiRequest<BalanceResultReq>, ApiResponse<BalanceResultRes>> {

    private final LoginInfoResolver loginInfoResolver;
    private final AccountDomain accountDomain;
    private final BalanceDomain balanceDomain;
    private final BatchResultStore batchResultStore;

    @Override
    public ApiResponse<BalanceResultRes> apply(ApiRequest<BalanceResultReq> req) {
        LoginInfo login = loginInfoResolver.current();
        var snapshot = batchResultStore.get(req.getBody().getBatchId())
                // 批次不是自己的也回「不存在」，不透露它其實存在
                .filter(s -> s.ownerCompanyId().equals(login.companyId()))
                .orElseThrow(() -> new BizException(ReturnCode.AGG_NOT_FOUND));
        var accounts = accountDomain.listAccounts(login.companyId(), null);
        return ApiResponse.ok(req.getHeader(), balanceDomain.summarize(snapshot, accounts));
    }
}
