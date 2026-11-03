package com.store.util;

import java.math.BigDecimal;

/**
 * Web 参数解析与输出工具。
 */
public final class WebUtil {

    private WebUtil() {
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static int toInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer toInteger(String value) {
        try {
            String v = trim(value);
            return v.isEmpty() ? null : Integer.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal toDecimal(String value) {
        try {
            String v = trim(value);
            return v.isEmpty() ? null : new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * CSV 单元格转义。
     */
    public static String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static String roleName(String role) {
        if ("admin".equals(role)) {
            return "管理员";
        }
        if ("manager".equals(role)) {
            return "店长";
        }
        if ("cashier".equals(role)) {
            return "收银员";
        }
        return role;
    }

    public static String statusName(String status) {
        if ("approved".equals(status)) {
            return "已通过";
        }
        if ("rejected".equals(status)) {
            return "已驳回";
        }
        return "待审核";
    }
}
