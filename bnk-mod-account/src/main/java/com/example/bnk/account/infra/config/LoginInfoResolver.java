package com.example.bnk.account.infra.config;

import com.example.bnk.shared.api.BizException;
import com.example.bnk.shared.api.ReturnCode;
import com.example.bnk.shared.login.LoginHeaders;
import com.example.bnk.shared.login.LoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 從 Gateway 轉入的內部 header 取得登入者 (由 Gateway 驗證 JWT 後填入)。 */
@Component
public class LoginInfoResolver {

    public LoginInfo current() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            throw new BizException(ReturnCode.NOT_LOGIN);
        }
        HttpServletRequest req = attrs.getRequest();
        String userId = req.getHeader(LoginHeaders.USER_ID);
        String companyId = req.getHeader(LoginHeaders.COMPANY_ID);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(companyId)) {
            throw new BizException(ReturnCode.NOT_LOGIN);
        }
        return new LoginInfo(userId, companyId);
    }
}
