package com.example.bnk.shared.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 統一回應信封：HTTP 一律 200，業務結果看 returnCode（銀行系統常見慣例）。 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private ApiHeader header;
    private String returnCode = ReturnCode.SUCCESS.getCode();
    private String returnMsg = ReturnCode.SUCCESS.getMessage();
    private T body;

    public ApiResponse(ApiHeader header) {
        this.header = header;
    }

    public static <T> ApiResponse<T> ok(ApiHeader header, T body) {
        ApiResponse<T> res = new ApiResponse<>(header);
        res.setBody(body);
        return res;
    }

    public static <T> ApiResponse<T> fail(ApiHeader header, ReturnCode code) {
        ApiResponse<T> res = new ApiResponse<>(header);
        res.setReturnCode(code.getCode());
        res.setReturnMsg(code.getMessage());
        return res;
    }
}
