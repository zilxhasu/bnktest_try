package com.example.bnk.svc;

import com.example.bnk.account.infra.config.AccountModuleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/** svc 本身幾乎沒有程式碼：只負責「把哪些 mod 組起來、怎麼設定、怎麼部署」。 */
@SpringBootApplication
@Import(AccountModuleConfig.class)
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
