package com.example.bnk.shared.login;

/**
 * Gateway 驗完 JWT 後，把身分放到這些內部 header 轉給後端服務。
 * 後端只信任來自 Gateway 的流量（正式環境應搭配 NetworkPolicy / mTLS）。
 */
public final class LoginHeaders {
    public static final String USER_ID = "X-Bnk-User-Id";
    public static final String COMPANY_ID = "X-Bnk-Company-Id";
    public static final String TRACE_ID = "X-Bnk-Trace-Id";

    private LoginHeaders() {
    }
}
