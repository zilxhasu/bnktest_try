package com.example.bnk.shared.login;

/**
 * 登入者資訊。
 *
 * @param userId 使用者代號
 * @param companyId   企業統一編號（同一統編下有多位使用者）
 */
public record LoginInfo(String userId, String companyId) {
}
