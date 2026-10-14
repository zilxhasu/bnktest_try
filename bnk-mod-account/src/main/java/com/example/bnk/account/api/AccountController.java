package com.example.bnk.account.api;

import com.example.bnk.account.usecase.ListAccountsUseCase;
import com.example.bnk.account.usecase.SaveAliasUseCase;
import com.example.bnk.account.infra.req.ListAccountsReq;
import com.example.bnk.account.infra.req.SaveAliasReq;
import com.example.bnk.account.infra.res.ListAccountsRes;
import com.example.bnk.account.infra.res.SaveAliasRes;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API 層：只做路由與驗證，不寫業務邏輯。一律 POST + JSON 信封。 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final ListAccountsUseCase listAccounts;
    private final SaveAliasUseCase saveAlias;

    @PostMapping("/list")
    public ApiResponse<ListAccountsRes> listAccounts(@Valid @RequestBody ApiRequest<ListAccountsReq> req) {
        return listAccounts.apply(req);
    }

    @PostMapping("/alias")
    public ApiResponse<SaveAliasRes> saveAlias(@Valid @RequestBody ApiRequest<SaveAliasReq> req) {
        return saveAlias.apply(req);
    }
}
