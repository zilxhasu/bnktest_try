package com.example.bnk.account.infra.req;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BalanceQueryReq {
    /** 要查餘額的帳號；空陣列 = 查此統編下全部帳號 */
    private List<String> accountNos = new ArrayList<>();
}
