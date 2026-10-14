package com.example.bnk.shared.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 核心主機回覆的電文。resultCode = 0000 代表成功。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreResponse {
    private String batchId;
    private int seq;
    private String txCode;
    private String area;
    private String resultCode;
    private Map<String, String> payload;
}
