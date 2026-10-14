package com.example.bnk.account.domain;

import com.example.bnk.account.infra.batch.BatchSnapshot;
import com.example.bnk.account.infra.db.entity.Account;
import com.example.bnk.account.infra.res.BalanceItem;
import com.example.bnk.account.infra.res.CurrencyTotal;
import com.example.bnk.account.infra.res.BalanceResultRes;
import com.example.bnk.shared.corebank.CoreRequest;
import com.example.bnk.shared.corebank.CoreResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 餘額總覽：一帳號一電文，回來後依幣別加總。純邏輯、無 I/O，最好寫單元測試。 */
@Component
public class BalanceDomain {

    public static final String TX_BALANCE = "BAL001";

    public List<CoreRequest> buildTelegrams(String batchId, List<Account> accounts) {
        List<CoreRequest> list = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            list.add(CoreRequest.builder()
                    .batchId(batchId)
                    .seq(i + 1)
                    .txCode(TX_BALANCE)
                    .area(a.getArea())
                    .payload(Map.of("accountNo", a.getAccountNo(), "currency", a.getCurrency()))
                    .build());
        }
        return list;
    }

    public BalanceResultRes summarize(BatchSnapshot snapshot, List<Account> accounts) {
        Map<String, Account> byAccount = accounts.stream()
                .collect(Collectors.toMap(Account::getAccountNo, Function.identity(), (x, y) -> x));

        List<BalanceItem> items = snapshot.results().stream()
                .sorted(Comparator.comparingInt(CoreResponse::getSeq))
                .map(r -> toItem(r, byAccount.get(r.getPayload().get("accountNo"))))
                .toList();

        Map<String, List<BalanceItem>> okByCurrency = new TreeMap<>(items.stream()
                .filter(i -> "OK".equals(i.status()))
                .collect(Collectors.groupingBy(BalanceItem::currency)));
        List<CurrencyTotal> totals = okByCurrency.entrySet().stream()
                .map(e -> new CurrencyTotal(e.getKey(),
                        e.getValue().stream().map(BalanceItem::balance).reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()))
                .toList();

        return new BalanceResultRes(snapshot.done(), snapshot.total(), items.size(), items, totals);
    }

    private BalanceItem toItem(CoreResponse r, Account account) {
        Map<String, String> p = r.getPayload();
        boolean ok = "0000".equals(r.getResultCode());
        return new BalanceItem(
                p.get("accountNo"),
                r.getArea(),
                p.getOrDefault("currency", account == null ? null : account.getCurrency()),
                account == null ? null : account.getAlias(),
                ok ? new BigDecimal(p.get("balance")) : null,
                ok ? "OK" : "FAIL");
    }
}
