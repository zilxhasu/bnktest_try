package com.example.bnk.shared.api;

import lombok.Getter;

/** 可預期的業務錯誤，由 GlobalExceptionHandler 轉成 ApiResponse。 */
@Getter
public class BizException extends RuntimeException {
    private final ReturnCode returnCode;

    public BizException(ReturnCode returnCode) {
        super(returnCode.getCode() + " " + returnCode.getMessage());
        this.returnCode = returnCode;
    }
}
