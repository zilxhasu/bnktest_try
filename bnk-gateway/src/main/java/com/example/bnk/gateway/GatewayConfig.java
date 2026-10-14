package com.example.bnk.gateway;

import io.smallrye.config.ConfigMapping;

import java.time.Duration;
import java.util.Map;

/** application.yaml 的 bnk.gateway.*：路由表與示範帳號都用設定檔管理，改路由不用改程式。 */
@ConfigMapping(prefix = "bnk.gateway")
public interface GatewayConfig {

    /** 服務代號 → 後端位址，例如 account → http://localhost:8081 */
    Map<String, String> routes();

    Duration tokenTtl();

    Duration upstreamTimeout();

    /** 示範用帳號（正式環境應改接 LDAP / IdP / 帳號服務） */
    Map<String, DemoUser> demoUsers();

    interface DemoUser {
        String companyId();

        String password();
    }
}
