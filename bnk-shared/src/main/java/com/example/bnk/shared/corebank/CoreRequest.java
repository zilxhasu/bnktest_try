package com.example.bnk.shared.corebank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 送往核心主機（Core Banking）的「電文」。
 * 真實銀行多為固定長度或 XML 格式；這裡用 JSON 表達同樣的概念。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreRequest {
    /** 聚合批次 ID：一次畫面查詢可能拆成多筆電文 */
    private String batchId;
    /** 批次內序號 */
    private int seq;
    /** 交易代號，例如 BAL001 = 餘額查詢 */
    private String txCode;
    /** 地區（TW/HK/SG/VN），決定送往哪一座核心主機 */
    private String area;
    private Map<String, String> payload;
}
