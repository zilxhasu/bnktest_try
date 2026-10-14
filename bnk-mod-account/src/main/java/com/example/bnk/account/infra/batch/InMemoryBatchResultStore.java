package com.example.bnk.account.infra.batch;

import com.example.bnk.account.infra.config.BnkProperties;
import com.example.bnk.shared.corebank.CoreResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** 單機版（bnk.batch.store=memory，預設）。只適合單一 Pod，多 Pod 請改 redis。 */
@Component
@ConditionalOnProperty(prefix = "bnk.batch", name = "store", havingValue = "memory", matchIfMissing = true)
public class InMemoryBatchResultStore implements BatchResultStore {

    private record Entry(String owner, int total, List<CoreResponse> results, Instant expireAt) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final int ttlSeconds;

    public InMemoryBatchResultStore(BnkProperties props) {
        this.ttlSeconds = props.getBatch().getTtlSeconds();
    }

    @Override
    public void create(String batchId, String ownerCompanyId, int total) {
        entries.values().removeIf(e -> e.expireAt().isBefore(Instant.now()));
        entries.put(batchId, new Entry(ownerCompanyId, total, new CopyOnWriteArrayList<>(), Instant.now().plusSeconds(ttlSeconds)));
    }

    @Override
    public void addResult(String batchId, CoreResponse response) {
        Entry e = entries.get(batchId);
        if (e != null) {
            e.results().add(response);
        }
    }

    @Override
    public Optional<BatchSnapshot> get(String batchId) {
        Entry e = entries.get(batchId);
        if (e == null || e.expireAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(new BatchSnapshot(e.owner(), e.total(), List.copyOf(e.results())));
    }
}
