package com.example.bnk.account.infra.db.entity;

import lombok.Data;

/** 對應資料表 COMPANY_ACCOUNT（LEFT JOIN ACCOUNT_ALIAS 取得別名）。 */
@Data
public class Account {
    private String companyId;
    private String accountNo;
    private String area;
    private String currency;
    private String accountType;
    private String alias;
}
