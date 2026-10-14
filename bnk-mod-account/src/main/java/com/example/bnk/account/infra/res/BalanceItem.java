package com.example.bnk.account.infra.res;

import java.math.BigDecimal;

public record BalanceItem(String accountNo, String area, String currency, String alias, BigDecimal balance, String status) {
}
