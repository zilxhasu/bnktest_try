package com.example.bnk.account.infra.res;

import java.math.BigDecimal;

public record CurrencyTotal(String currency, BigDecimal total, int count) {
}
