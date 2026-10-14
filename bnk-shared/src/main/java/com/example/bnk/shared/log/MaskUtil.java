package com.example.bnk.shared.log;

/** 個資遮罩：寫 log 前一律遮罩帳號、統編，避免敏感資料外洩到日誌系統。 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 保留前 3 碼與後 3 碼，中間以 * 取代。 */
    public static String maskAccount(String account) {
        if (account == null || account.length() <= 6) {
            return "******";
        }
        return account.substring(0, 3) + "*".repeat(account.length() - 6) + account.substring(account.length() - 3);
    }

    /** 統編只保留後 2 碼。 */
    public static String maskCompanyId(String companyId) {
        if (companyId == null || companyId.length() <= 2) {
            return "**";
        }
        return "*".repeat(companyId.length() - 2) + companyId.substring(companyId.length() - 2);
    }
}
