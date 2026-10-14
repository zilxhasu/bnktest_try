package com.example.bnk.account.infra.req;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ListAccountsReq {
    /** 選填：只查某地區 */
    @Pattern(regexp = "TW|HK|SG|VN")
    private String area;
}
