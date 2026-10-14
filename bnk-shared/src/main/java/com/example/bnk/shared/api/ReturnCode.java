package com.example.bnk.shared.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 業務回應碼。前端依代碼查 i18n 顯示訊息。 */
@Getter
@AllArgsConstructor
public enum ReturnCode {
    SUCCESS("0000", "成功"),
    INVALID_INPUT("E001", "輸入資料有誤"),
    NOT_LOGIN("E002", "尚未登入或登入逾時"),
    ACCOUNT_NOT_AUTHORIZED("E003", "無此帳號權限"),
    AGG_NOT_FOUND("E004", "查詢批次不存在或已逾期"),
    CORE_TIMEOUT("E900", "主機回應逾時"),
    SYSTEM_ERROR("E999", "系統忙碌中，請稍後再試");

    private final String code;
    private final String message;
}
