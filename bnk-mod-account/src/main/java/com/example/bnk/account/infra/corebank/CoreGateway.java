package com.example.bnk.account.infra.corebank;

import com.example.bnk.shared.corebank.CoreRequest;

/** 發送電文的抽象。送出即返回（fire-and-forget），回覆由 CoreResponseHandler 非同步接收。 */
public interface CoreGateway {
    void send(CoreRequest request);
}
