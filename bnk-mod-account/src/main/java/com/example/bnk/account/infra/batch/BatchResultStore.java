package com.example.bnk.account.infra.batch;

import com.example.bnk.shared.corebank.CoreResponse;

import java.util.Optional;

/** 「先發電文、再輪詢取結果」的暫存區。 */
public interface BatchResultStore {

    void create(String batchId, String ownerCompanyId, int total);

    void addResult(String batchId, CoreResponse response);

    Optional<BatchSnapshot> get(String batchId);
}
