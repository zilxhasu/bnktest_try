package com.example.bnk.emulator;

import com.example.bnk.shared.corebank.CoreQueues;
import com.example.bnk.shared.corebank.CoreRequest;
import com.example.bnk.shared.corebank.CoreResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 假主機：讀 resources/simulate/{txCode}.json 範本，把 ${xxx} 換成請求值或隨機值後回覆。
 * 想新增交易，只要多放一個 json 範本即可。
 */
@Slf4j
@Component
public class CoreHostSimulator {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final int maxDelayMs;
    private final double failRate;

    public CoreHostSimulator(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper,
                             @Value("${emulator.max-delay-ms:1500}") int maxDelayMs,
                             @Value("${emulator.fail-rate:0.05}") double failRate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.maxDelayMs = maxDelayMs;
        this.failRate = failRate;
    }

    @RabbitListener(queues = {"bnk.core.req.TW", "bnk.core.req.HK", "bnk.core.req.SG", "bnk.core.req.VN"},
            concurrency = "4")
    public void onRequest(CoreRequest req) throws Exception {
        Thread.sleep(ThreadLocalRandom.current().nextLong(Math.max(1, maxDelayMs)));
        boolean fail = ThreadLocalRandom.current().nextDouble() < failRate;
        CoreResponse resp = CoreResponse.builder()
                .batchId(req.getBatchId())
                .seq(req.getSeq())
                .txCode(req.getTxCode())
                .area(req.getArea())
                .resultCode(fail ? "E101" : "0000")
                .payload(fail ? Map.of("accountNo", req.getPayload().get("accountNo")) : render(req))
                .build();
        log.info("[{}] {} seq={} -> {}", req.getArea(), req.getTxCode(), req.getSeq(), resp.getResultCode());
        rabbitTemplate.convertAndSend(CoreQueues.EXCHANGE, CoreQueues.RESP_ROUTING_KEY, resp);
    }

    Map<String, String> render(CoreRequest req) throws IOException {
        Map<String, String> template;
        try (InputStream in = new ClassPathResource("simulate/" + req.getTxCode() + ".json").getInputStream()) {
            template = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
        String balance = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1_000, 5_000_000))
                .setScale(2, RoundingMode.HALF_UP).toPlainString();
        Map<String, String> out = new HashMap<>();
        template.forEach((k, v) -> {
            String val = v.replace("${random.balance}", balance);
            for (var e : req.getPayload().entrySet()) {
                val = val.replace("${" + e.getKey() + "}", e.getValue());
            }
            out.put(k, val);
        });
        return out;
    }
}
