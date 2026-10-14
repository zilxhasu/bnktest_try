package com.example.bnk.account.infra.db.mapper;

import com.example.bnk.account.infra.db.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** MyBatis Mapper：SQL 寫在 resources/mappers/AccountMapper.xml，DBA 可直接審閱與調校。 */
@Mapper
public interface AccountMapper {

    List<Account> selectByCompanyId(@Param("companyId") String companyId);

    List<Account> selectByCompanyIdAndAccounts(@Param("companyId") String companyId, @Param("accountNos") List<String> accountNos);

    int updateAlias(@Param("companyId") String companyId, @Param("accountNo") String accountNo, @Param("alias") String alias);

    int insertAlias(@Param("companyId") String companyId, @Param("accountNo") String accountNo, @Param("alias") String alias);
}
