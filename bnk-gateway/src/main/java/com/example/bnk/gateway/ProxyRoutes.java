package com.example.bnk.gateway;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bnk.shared.login.LoginHeaders;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * 反向代理：/api/{svc}/** → routes[svc]/**
 * 流程：驗 JWT（Authenticator）→ 找路由（Router）→ 加內部身分 header → 非阻塞轉發（Vert.x WebClient）。
 * 進階做法可把每一步做成可插拔的 Plugin；這裡用最精簡的寫法示範整條鏈。
 */
@ApplicationScoped
public class ProxyRoutes {

    private static final Logger LOG = Logger.getLogger(ProxyRoutes.class);
    private static final String PREFIX = "/api/";

    private final GatewayConfig config;
    private final JwtService jwtService;
    private final Vertx vertx;
    private WebClient client;

    public ProxyRoutes(GatewayConfig config, JwtService jwtService, Vertx vertx) {
        this.config = config;
        this.jwtService = jwtService;
        this.vertx = vertx;
    }

    void init(@Observes Router router) {
        client = WebClient.create(vertx, new WebClientOptions()
                .setConnectTimeout(5_000)
                .setIdleTimeout(60)
                .setMaxPoolSize(50));
        router.route(PREFIX + "*")
                .handler(BodyHandler.create().setBodyLimit(1024 * 1024))
                .handler(this::proxy);
    }

    void proxy(RoutingContext ctx) {
        // 1. Authenticator：驗 Bearer token
        String auth = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            reply(ctx, 401, "E002", "尚未登入");
            return;
        }
        DecodedJWT jwt;
        try {
            jwt = jwtService.verify(auth.substring(7));
        } catch (JWTVerificationException e) {
            reply(ctx, 401, "E002", "登入逾時或憑證無效");
            return;
        }

        // 2. Router：/api/{svc}/rest → target + /rest
        String path = ctx.request().path().substring(PREFIX.length());
        int slash = path.indexOf('/');
        String svc = slash < 0 ? path : path.substring(0, slash);
        String target = config.routes().get(svc);
        if (target == null || slash < 0) {
            reply(ctx, 404, "E404", "查無此服務");
            return;
        }
        String upstreamPath = path.substring(slash);
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // 3. 轉發：內部 header 由 gateway 重新產生，不沿用前端送來的值（防偽造身分）
        HttpRequest<io.vertx.core.buffer.Buffer> upstream = client
                .requestAbs(ctx.request().method(), target + upstreamPath)
                .timeout(config.upstreamTimeout().toMillis())
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .putHeader(LoginHeaders.USER_ID, jwt.getSubject())
                .putHeader(LoginHeaders.COMPANY_ID, jwt.getClaim("companyId").asString())
                .putHeader(LoginHeaders.TRACE_ID, traceId);

        long start = System.nanoTime();
        upstream.sendBuffer(ctx.body().buffer() == null ? io.vertx.core.buffer.Buffer.buffer() : ctx.body().buffer())
                .onSuccess(res -> {
                    LOG.infof("trace=%s %s %s -> %d (%d ms)", traceId, svc, upstreamPath, res.statusCode(),
                            (System.nanoTime() - start) / 1_000_000);
                    ctx.response().setStatusCode(res.statusCode());
                    String ct = res.getHeader(HttpHeaders.CONTENT_TYPE.toString());
                    if (ct != null) {
                        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ct);
                    }
                    ctx.response().putHeader(LoginHeaders.TRACE_ID, traceId);
                    ctx.response().end(res.body() == null ? io.vertx.core.buffer.Buffer.buffer() : res.body());
                })
                .onFailure(err -> {
                    LOG.errorf("trace=%s upstream %s failed: %s", traceId, svc, err.getMessage());
                    reply(ctx, 502, "E999", "系統忙碌中，請稍後再試");
                });
    }

    private static void reply(RoutingContext ctx, int status, String code, String msg) {
        ctx.response().setStatusCode(status)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .end(new JsonObject().put("returnCode", code).put("returnMsg", msg).encode());
    }
}
