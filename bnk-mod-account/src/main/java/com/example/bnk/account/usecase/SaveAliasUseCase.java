package com.example.bnk.account.usecase;

import com.example.bnk.account.domain.AccountDomain;
import com.example.bnk.account.infra.config.LoginInfoResolver;
import com.example.bnk.account.infra.req.SaveAliasReq;
import com.example.bnk.account.infra.res.SaveAliasRes;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SaveAliasUseCase implements Function<ApiRequest<SaveAliasReq>, ApiResponse<SaveAliasRes>> {

    private final LoginInfoResolver loginInfoResolver;
    private final AccountDomain accountDomain;

    @Override
    public ApiResponse<SaveAliasRes> apply(ApiRequest<SaveAliasReq> req) {
        String companyId = loginInfoResolver.current().companyId();
        SaveAliasReq body = req.getBody();
        accountDomain.saveAlias(companyId, body.getAccountNo(), body.getAlias().trim());
        return ApiResponse.ok(req.getHeader(), new SaveAliasRes(body.getAccountNo(), body.getAlias().trim()));
    }
}
