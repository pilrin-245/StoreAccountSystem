package com.store.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MD5 密码工具单元测试。
 */
public class MD5UtilTest {

    @Test
    public void testMd5Of123456() {
        assertEquals("e10adc3949ba59abbe56e057f20f883e", MD5Util.md5("123456"));
    }

    @Test
    public void testMatches() {
        String encrypted = MD5Util.md5("abc123");
        assertTrue(MD5Util.matches("abc123", encrypted));
        assertFalse(MD5Util.matches("abc124", encrypted));
    }

    @Test
    public void testNullSafe() {
        assertEquals(null, MD5Util.md5(null));
        assertFalse(MD5Util.matches(null, "e10adc3949ba59abbe56e057f20f883e"));
    }
}
