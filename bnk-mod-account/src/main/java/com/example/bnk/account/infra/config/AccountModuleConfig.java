package com.example.bnk.account.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 模組進入點：svc 只要 @Import 這個類別就能把整個業務模組掛進來，
 * 同一個 mod 也能被不同 svc 重複組裝。
 */
@Configuration
@ComponentScan(basePackages = "com.example.bnk.account")
@MapperScan(basePackages = "com.example.bnk.account.infra.db.mapper")
@EnableConfigurationProperties(BnkProperties.class)
public class AccountModuleConfig {
}
