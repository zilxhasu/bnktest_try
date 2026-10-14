package com.example.bnk.account.infra.config;

import com.example.bnk.shared.api.ApiResponse;
import com.example.bnk.shared.api.BizException;
import com.example.bnk.shared.api.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 例外統一轉成 ApiResponse，不把 stack trace 回給前端（資安弱掃常見要求）。 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e) {
        log.warn("biz error: {}", e.getMessage());
        return ApiResponse.fail(null, e.getReturnCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleInvalid(Exception e) {
        log.warn("invalid input: {}", e.getClass().getSimpleName());
        return ApiResponse.fail(null, ReturnCode.INVALID_INPUT);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        log.error("unexpected error", e);
        return ApiResponse.fail(null, ReturnCode.SYSTEM_ERROR);
    }
}
