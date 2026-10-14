package com.example.bnk.shared.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 每支 API 共用的請求表頭：交易追蹤序號、通路、語系。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeader {
    private String txnId;
    private String channel;
    private String locale;
}
