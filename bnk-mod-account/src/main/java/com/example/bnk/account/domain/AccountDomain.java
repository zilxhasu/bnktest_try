package com.example.bnk.account.domain;

import com.example.bnk.account.infra.db.entity.Account;
import com.example.bnk.account.infra.db.mapper.AccountMapper;
import com.example.bnk.shared.api.BizException;
import com.example.bnk.shared.api.ReturnCode;
import com.example.bnk.shared.log.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/** 帳戶相關業務規則：可被多支 UseCase 重複使用。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDomain {

    private final AccountMapper accountMapper;

    public List<Account> listAccounts(String companyId, String area) {
        List<Account> all = accountMapper.selectByCompanyId(companyId);
        return area == null ? all : all.stream().filter(a -> area.equals(a.getArea())).toList();
    }

    /**
     * 越權檢查：請求中的每個帳號都必須屬於登入者統編。
     * 前端送來的帳號一律不可信任（OWASP A01 Broken Access Control）。
     */
    public List<Account> requireOwned(String companyId, List<String> accountNos) {
        if (accountNos == null || accountNos.isEmpty()) {
            return accountMapper.selectByCompanyId(companyId);
        }
        List<Account> owned = accountMapper.selectByCompanyIdAndAccounts(companyId, accountNos);
        if (owned.size() != new HashSet<>(accountNos).size()) {
            log.warn("unauthorized account access, companyId={}", MaskUtil.maskCompanyId(companyId));
            throw new BizException(ReturnCode.ACCOUNT_NOT_AUTHORIZED);
        }
        return owned;
    }

    @Transactional
    public void saveAlias(String companyId, String accountNo, String alias) {
        requireOwned(companyId, List.of(accountNo));
        if (accountMapper.updateAlias(companyId, accountNo, alias) == 0) {
            accountMapper.insertAlias(companyId, accountNo, alias);
        }
        log.info("alias saved account={}", MaskUtil.maskAccount(accountNo));
    }
}
