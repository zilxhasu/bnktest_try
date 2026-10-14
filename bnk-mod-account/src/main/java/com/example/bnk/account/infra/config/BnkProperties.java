package com.example.bnk.account.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 專案自訂設定（application.yml 的 bnk.*）。
 * 透過「模式切換」讓同一份程式碼可以：
 * - 本機零依賴執行（simulate + memory）
 * - docker / k8s 走真實 RabbitMQ + Redis（mq + redis）
 */
@Data
@ConfigurationProperties(prefix = "bnk")
public class BnkProperties {

    private Corebank corebank = new Corebank();
    private Batch batch = new Batch();

    @Data
    public static class Corebank {
        /** simulate：程式內模擬主機；mq：經 RabbitMQ 送往主機模擬器 */
        private String mode = "simulate";
        /** simulate 模式下，模擬主機回應延遲（毫秒）上限 */
        private int simulateMaxDelayMs = 800;
    }

    @Data
    public static class Batch {
        /** memory：單機 Map；redis：多 Pod 共用 */
        private String store = "memory";
        private int ttlSeconds = 300;
        /** 建議前端輪詢間隔 */
        private int pollIntervalMs = 500;
    }
}
