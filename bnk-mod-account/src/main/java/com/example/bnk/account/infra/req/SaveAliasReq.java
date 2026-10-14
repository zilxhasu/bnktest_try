package com.example.bnk.account.infra.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveAliasReq {
    @NotBlank
    @Pattern(regexp = "\\d{6,17}")
    private String accountNo;

    @NotBlank
    @Size(max = 15)
    private String alias;
}
