package com.example.bnk.shared.api;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 統一請求信封：{ "header": {...}, "body": {...} }。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest<T> {
    private ApiHeader header = new ApiHeader();
    /** @Valid 讓 Controller 的驗證能串到 body 內欄位 */
    @Valid
    private T body;
}
