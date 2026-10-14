package com.example.bnk.account;

import com.example.bnk.account.domain.BalanceDomain;
import com.example.bnk.account.infra.batch.BatchSnapshot;
import com.example.bnk.account.infra.db.entity.Account;
import com.example.bnk.shared.corebank.CoreResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceDomainTest {

    private final BalanceDomain domain = new BalanceDomain();

    private static Account account(String no, String area, String currency) {
        Account a = new Account();
        a.setAccountNo(no);
        a.setArea(area);
        a.setCurrency(currency);
        return a;
    }

    private static CoreResponse resp(int seq, String account, String currency, String rtn, String bal) {
        return CoreResponse.builder().batchId("A").seq(seq).area("TW").resultCode(rtn)
                .payload(Map.of("accountNo", account, "currency", currency, "balance", bal)).build();
    }

    @Test
    void buildOneCoreRequestPerAccount() {
        var tgs = domain.buildTelegrams("A", List.of(account("111", "TW", "TWD"), account("222", "HK", "HKD")));
        assertThat(tgs).hasSize(2);
        assertThat(tgs.get(1).getSeq()).isEqualTo(2);
        assertThat(tgs.get(1).getArea()).isEqualTo("HK");
        assertThat(tgs.get(0).getTxCode()).isEqualTo(BalanceDomain.TX_BALANCE);
    }

    @Test
    void summarizeGroupsByCurrencyAndSkipsFailures() {
        var accounts = List.of(account("111", "TW", "TWD"), account("222", "TW", "TWD"), account("333", "TW", "USD"));
        var snapshot = new BatchSnapshot("12345678", 4, List.of(
                resp(2, "222", "TWD", "0000", "50.50"),
                resp(1, "111", "TWD", "0000", "100.00"),
                resp(3, "333", "USD", "9999", "0")));

        var res = domain.summarize(snapshot, accounts);

        assertThat(res.done()).isFalse();
        assertThat(res.received()).isEqualTo(3);
        assertThat(res.items().get(0).accountNo()).isEqualTo("111");
        assertThat(res.items().get(2).status()).isEqualTo("FAIL");
        assertThat(res.totals()).hasSize(1);
        assertThat(res.totals().get(0).total()).isEqualByComparingTo(new BigDecimal("150.50"));
    }
}
