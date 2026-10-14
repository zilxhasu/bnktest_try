package com.example.bnk.account.usecase;

import com.example.bnk.account.domain.AccountDomain;
import com.example.bnk.account.infra.config.LoginInfoResolver;
import com.example.bnk.account.infra.req.ListAccountsReq;
import com.example.bnk.account.infra.res.AccountItem;
import com.example.bnk.account.infra.res.ListAccountsRes;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import com.example.bnk.shared.login.LoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/** UseCase 層：一支 API 一個類別，只負責「流程編排」，規則交給 domain。 */
@Service
@RequiredArgsConstructor
public class ListAccountsUseCase implements Function<ApiRequest<ListAccountsReq>, ApiResponse<ListAccountsRes>> {

    private final LoginInfoResolver loginInfoResolver;
    private final AccountDomain accountDomain;

    @Override
    public ApiResponse<ListAccountsRes> apply(ApiRequest<ListAccountsReq> req) {
        LoginInfo login = loginInfoResolver.current();
        String area = req.getBody() == null ? null : req.getBody().getArea();
        var accounts = accountDomain.listAccounts(login.companyId(), area).stream()
                .map(a -> new AccountItem(a.getAccountNo(), a.getArea(), a.getCurrency(), a.getAccountType(), a.getAlias()))
                .toList();
        return ApiResponse.ok(req.getHeader(), new ListAccountsRes(accounts));
    }
}
