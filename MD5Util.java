package com.store.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 密码工具类。
 */
public final class MD5Util {

    private MD5Util() {
    }

    public static String md5(String text) {
        if (text == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    public static boolean matches(String rawPassword, String encrypted) {
        return md5(rawPassword) != null && md5(rawPassword).equalsIgnoreCase(encrypted);
    }
}
