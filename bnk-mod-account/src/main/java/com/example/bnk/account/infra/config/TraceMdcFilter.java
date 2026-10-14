package com.example.bnk.account.infra.config;

import com.example.bnk.shared.log.MaskUtil;
import com.example.bnk.shared.login.LoginHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** 把 traceId / userId 放進 MDC，每一行 log 都帶得到，方便在集中式日誌串查同一筆交易。 */
@Component
public class TraceMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String traceId = req.getHeader(LoginHeaders.TRACE_ID);
        MDC.put("traceId", traceId != null ? traceId : UUID.randomUUID().toString().substring(0, 8));
        MDC.put("userId", String.valueOf(req.getHeader(LoginHeaders.USER_ID)));
        MDC.put("companyId", MaskUtil.maskCompanyId(req.getHeader(LoginHeaders.COMPANY_ID)));
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
