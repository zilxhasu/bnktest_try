package com.example.bnk.account.infra.batch;

import com.example.bnk.account.infra.config.BnkProperties;
import com.example.bnk.shared.corebank.CoreResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis 版（bnk.batch.store=redis）。
 * batch:{id}:meta    Hash  { owner, total }
 * batch:{id}:results List  [ CoreResponse JSON ... ]
 * 兩個 key 都設 TTL，過期自動清掉；任何一個 Pod 收到回覆都能寫入，任何一個 Pod 都能讀。
 */
@Component
@ConditionalOnProperty(prefix = "bnk.batch", name = "store", havingValue = "redis")
public class RedisBatchResultStore implements BatchResultStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisBatchResultStore(StringRedisTemplate redis, ObjectMapper objectMapper, BnkProperties props) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(props.getBatch().getTtlSeconds());
    }

    private static String metaKey(String batchId) {
        return "batch:" + batchId + ":meta";
    }

    private static String resultsKey(String batchId) {
        return "batch:" + batchId + ":results";
    }

    @Override
    public void create(String batchId, String ownerCompanyId, int total) {
        redis.opsForHash().putAll(metaKey(batchId), Map.of("owner", ownerCompanyId, "total", String.valueOf(total)));
        redis.expire(metaKey(batchId), ttl);
    }

    @Override
    public void addResult(String batchId, CoreResponse response) {
        try {
            redis.opsForList().rightPush(resultsKey(batchId), objectMapper.writeValueAsString(response));
            redis.expire(resultsKey(batchId), ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("serialize corebank response failed", e);
        }
    }

    @Override
    public Optional<BatchSnapshot> get(String batchId) {
        Map<Object, Object> meta = redis.opsForHash().entries(metaKey(batchId));
        if (meta.isEmpty()) {
            return Optional.empty();
        }
        List<String> raw = redis.opsForList().range(resultsKey(batchId), 0, -1);
        List<CoreResponse> results = raw == null ? List.of() : raw.stream().map(this::parse).toList();
        return Optional.of(new BatchSnapshot((String) meta.get("owner"), Integer.parseInt((String) meta.get("total")), results));
    }

    private CoreResponse parse(String json) {
        try {
            return objectMapper.readValue(json, CoreResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("parse corebank response failed", e);
        }
    }
}
