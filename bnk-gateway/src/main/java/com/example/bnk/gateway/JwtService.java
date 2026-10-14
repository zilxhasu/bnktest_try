package com.example.bnk.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.enterprise.context.ApplicationScoped;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

/**
 * 簽發 / 驗證 JWT（RS256 非對稱金鑰）。
 * 示範專案在啟動時「臨時產生」金鑰對，所以 Git 裡沒有任何私鑰；
 * 正式環境應改從 K8s Secret / Vault / HSM 載入，而且多個 Pod 要用同一把。
 */
@ApplicationScoped
public class JwtService {

    private static final String ISSUER = "bnk-gateway";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final GatewayConfig config;

    public JwtService(GatewayConfig config) throws NoSuchAlgorithmException {
        this.config = config;
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        this.algorithm = Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate());
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).acceptLeeway(5).build();
    }

    public String issue(String userId, String companyId) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("companyId", companyId)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(config.tokenTtl()))
                .sign(algorithm);
    }

    /** 驗證失敗（簽章錯、過期、竄改）會丟 JWTVerificationException。 */
    public DecodedJWT verify(String token) {
        return verifier.verify(token);
    }

    public long ttlSeconds() {
        return config.tokenTtl().toSeconds();
    }
}
