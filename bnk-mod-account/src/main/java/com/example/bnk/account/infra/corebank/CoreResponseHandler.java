package com.example.bnk.account.infra.corebank;

import com.example.bnk.account.infra.batch.BatchResultStore;
import com.example.bnk.shared.corebank.CoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 收到主機回覆後，寫入聚合暫存區，等前端輪詢取回。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoreResponseHandler {

    private final BatchResultStore batchResultStore;

    public void onResponse(CoreResponse response) {
        log.debug("corebank resp batchId={} seq={} rtn={}", response.getBatchId(), response.getSeq(), response.getResultCode());
        batchResultStore.addResult(response.getBatchId(), response);
    }
}
