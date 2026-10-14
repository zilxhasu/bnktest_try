package com.example.bnk.gateway;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** POST /auth/login：驗證帳密，成功發 JWT。 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class);

    public record LoginReq(String userId, String password) {
    }

    private final GatewayConfig config;
    private final JwtService jwtService;

    public LoginResource(GatewayConfig config, JwtService jwtService) {
        this.config = config;
        this.jwtService = jwtService;
    }

    @POST
    @Path("/login")
    public Response login(LoginReq req) {
        if (req == null || req.userId() == null || req.password() == null) {
            return error(Response.Status.BAD_REQUEST, "E001", "輸入資料有誤");
        }
        GatewayConfig.DemoUser user = config.demoUsers().get(req.userId());
        // 帳號不存在與密碼錯誤回同樣訊息，避免帳號列舉；比對用常數時間避免 timing attack
        if (user == null || !MessageDigest.isEqual(
                user.password().getBytes(StandardCharsets.UTF_8), req.password().getBytes(StandardCharsets.UTF_8))) {
            LOG.warnf("login failed user=%s", req.userId());
            return error(Response.Status.UNAUTHORIZED, "E002", "帳號或密碼錯誤");
        }
        String token = jwtService.issue(req.userId(), user.companyId());
        return Response.ok(Map.of(
                "returnCode", "0000",
                "token", token,
                "expiresIn", jwtService.ttlSeconds(),
                "userId", req.userId())).build();
    }

    private static Response error(Response.Status status, String code, String msg) {
        return Response.status(status).entity(Map.of("returnCode", code, "returnMsg", msg)).build();
    }
}
