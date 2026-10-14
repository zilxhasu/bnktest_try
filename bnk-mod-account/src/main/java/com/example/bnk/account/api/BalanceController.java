package com.example.bnk.account.api;

import com.example.bnk.account.usecase.GetBalanceResultUseCase;
import com.example.bnk.account.usecase.StartBalanceQueryUseCase;
import com.example.bnk.account.infra.req.BalanceResultReq;
import com.example.bnk.account.infra.req.BalanceQueryReq;
import com.example.bnk.account.infra.res.BalanceResultRes;
import com.example.bnk.account.infra.res.BalanceQueryRes;
import com.example.bnk.shared.api.ApiRequest;
import com.example.bnk.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final StartBalanceQueryUseCase startQuery;
    private final GetBalanceResultUseCase getResult;

    @PostMapping("/query")
    public ApiResponse<BalanceQueryRes> startQuery(@Valid @RequestBody ApiRequest<BalanceQueryReq> req) {
        return startQuery.apply(req);
    }

    @PostMapping("/result")
    public ApiResponse<BalanceResultRes> getResult(@Valid @RequestBody ApiRequest<BalanceResultReq> req) {
        return getResult.apply(req);
    }
}
