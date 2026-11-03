package com.store.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 表单令牌工具：防止重复提交。
 */
public final class TokenUtil {

    public static final String SESSION_KEY = "formToken";
    public static final String PARAM_KEY = "_token";

    private TokenUtil() {
    }

    /**
     * 生成一次性令牌并放入 Session，页面通过 ${formToken} 使用。
     */
    public static String generate(HttpSession session) {
        String token = UUID.randomUUID().toString().replace("-", "");
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    /**
     * 校验并消费令牌。
     */
    public static boolean verify(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_KEY);
        String actual = request.getParameter(PARAM_KEY);
        if (expected != null && expected.toString().equals(actual)) {
            session.removeAttribute(SESSION_KEY);
            return true;
        }
        return false;
    }
}
