package com.example.bnk.shared.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaskUtilTest {

    @Test
    void maskAccountKeepsHeadAndTail() {
        assertEquals("001******789", MaskUtil.maskAccount("001234567789"));
    }

    @Test
    void maskShortValues() {
        assertEquals("******", MaskUtil.maskAccount("123"));
        assertEquals("******78", MaskUtil.maskCompanyId("12345678"));
    }
}
