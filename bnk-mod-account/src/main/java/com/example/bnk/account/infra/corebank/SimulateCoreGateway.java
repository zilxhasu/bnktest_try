package com.example.bnk.account.infra.corebank;

import com.example.bnk.account.infra.config.BnkProperties;
import com.example.bnk.shared.corebank.CoreRequest;
import com.example.bnk.shared.corebank.CoreResponse;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 模擬主機（bnk.corebank.mode=simulate，預設）。
 * 不需要 RabbitMQ 也不需要主機，在程式內延遲數百毫秒後「假裝」主機回覆，
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "bnk.corebank", name = "mode", havingValue = "simulate", matchIfMissing = true)
public class SimulateCoreGateway implements CoreGateway {

    private final CoreResponseHandler handler;
    private final int maxDelayMs;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public SimulateCoreGateway(CoreResponseHandler handler, BnkProperties props) {
        this.handler = handler;
        this.maxDelayMs = Math.max(1, props.getCorebank().getSimulateMaxDelayMs());
        log.info("Corebank mode = SIMULATE（不連線主機）");
    }

    @Override
    public void send(CoreRequest req) {
        long delay = ThreadLocalRandom.current().nextLong(maxDelayMs);
        scheduler.schedule(() -> handler.onResponse(fakeResponse(req)), delay, TimeUnit.MILLISECONDS);
    }

    static CoreResponse fakeResponse(CoreRequest req) {
        BigDecimal balance = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1_000, 5_000_000))
                .setScale(2, RoundingMode.HALF_UP);
        return CoreResponse.builder()
                .batchId(req.getBatchId())
                .seq(req.getSeq())
                .txCode(req.getTxCode())
                .area(req.getArea())
                .resultCode("0000")
                .payload(Map.of(
                        "accountNo", req.getPayload().get("accountNo"),
                        "currency", req.getPayload().getOrDefault("currency", "TWD"),
                        "balance", balance.toPlainString(),
                        "availBalance", balance.toPlainString()))
                .build();
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
