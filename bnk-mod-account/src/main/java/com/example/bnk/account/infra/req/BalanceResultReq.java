package com.example.bnk.account.infra.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BalanceResultReq {
    @NotBlank
    private String batchId;
}
