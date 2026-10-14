package com.example.bnk.shared.corebank;

/** RabbitMQ 名稱集中管理，服務端與主機模擬器共用。 */
public final class CoreQueues {
    /** topic exchange，routing key = core.req.{AREA} */
    public static final String EXCHANGE = "bnk.core";
    public static final String REQ_ROUTING_PREFIX = "core.req.";
    public static final String REQ_QUEUE_PREFIX = "bnk.core.req.";
    /** 主機回覆統一送到這個 queue */
    public static final String RESP_ROUTING_KEY = "core.resp";
    public static final String RESP_QUEUE = "bnk.core.resp";

    private CoreQueues() {
    }
}
