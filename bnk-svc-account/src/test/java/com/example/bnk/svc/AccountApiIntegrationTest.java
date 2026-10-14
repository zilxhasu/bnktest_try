package com.example.bnk.svc;

import com.example.bnk.shared.login.LoginHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 啟動完整 Spring Context（H2 + simulate 模式），從 HTTP 打到 DB 驗證整條路。 */
@SpringBootTest(properties = "bnk.corebank.simulate-max-delay-ms=50")
@AutoConfigureMockMvc
class AccountApiIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private JsonNode call(String path, String body, String companyId) throws Exception {
        var req = post(path).contentType(MediaType.APPLICATION_JSON)
                .content("{\"header\":{\"txnId\":\"t1\"},\"body\":" + body + "}");
        if (companyId != null) {
            req.header(LoginHeaders.USER_ID, "demo").header(LoginHeaders.COMPANY_ID, companyId);
        }
        String json = mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(json);
    }

    @Test
    void listAccountsOfLoginCompanyOnly() throws Exception {
        JsonNode res = call("/accounts/list", "{}", "12345678");
        assertThat(res.path("returnCode").asText()).isEqualTo("0000");
        assertThat(res.path("body").path("accounts")).hasSize(6);
    }

    @Test
    void rejectWithoutLogin() throws Exception {
        JsonNode res = call("/accounts/list", "{}", null);
        assertThat(res.path("returnCode").asText()).isEqualTo("E002");
    }

    @Test
    void rejectOtherCompanysAccount() throws Exception {
        JsonNode res = call("/balances/query", "{\"accountNos\":[\"001100000099\"]}", "12345678");
        assertThat(res.path("returnCode").asText()).isEqualTo("E003");
    }

    @Test
    void validationErrorOnBadAlias() throws Exception {
        JsonNode res = call("/accounts/alias", "{\"accountNo\":\"abc\",\"alias\":\"\"}", "12345678");
        assertThat(res.path("returnCode").asText()).isEqualTo("E001");
    }

    @Test
    void saveAliasThenReadBack() throws Exception {
        call("/accounts/alias", "{\"accountNo\":\"001200000003\",\"alias\":\"美金備用\"}", "12345678");
        JsonNode res = call("/accounts/list", "{\"area\":\"TW\"}", "12345678");
        assertThat(res.toString()).contains("美金備用");
    }

    @Test
    void prepareThenPollUntilDone() throws Exception {
        JsonNode prep = call("/balances/query", "{\"accountNos\":[]}", "12345678");
        String batchId = prep.path("body").path("batchId").asText();
        assertThat(prep.path("body").path("total").asInt()).isEqualTo(6);

        JsonNode sum = null;
        for (int i = 0; i < 50; i++) {
            sum = call("/balances/result", "{\"batchId\":\"" + batchId + "\"}", "12345678");
            if (sum.path("body").path("done").asBoolean()) {
                break;
            }
            Thread.sleep(50);
        }
        assertThat(sum.path("body").path("done").asBoolean()).isTrue();
        assertThat(sum.path("body").path("items")).hasSize(6);
        assertThat(sum.path("body").path("totals").size()).isEqualTo(4); // HKD SGD TWD USD

        // 別家公司拿到 batchId 也讀不到
        JsonNode other = call("/balances/result", "{\"batchId\":\"" + batchId + "\"}", "87654321");
        assertThat(other.path("returnCode").asText()).isEqualTo("E004");
    }
}
