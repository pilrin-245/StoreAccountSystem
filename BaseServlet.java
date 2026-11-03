package com.store.servlet;

import com.store.entity.User;
import com.store.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servlet 基类：参数解析、转发、重定向、分页等公共方法。
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("loginUser");
    }

    protected String param(HttpServletRequest request, String name) {
        return WebUtil.trim(request.getParameter(name));
    }

    protected String param(HttpServletRequest request, String name, String defaultValue) {
        String value = param(request, name);
        return value.isEmpty() ? defaultValue : value;
    }

    protected int intParam(HttpServletRequest request, String name, int defaultValue) {
        return WebUtil.toInt(request.getParameter(name), defaultValue);
    }

    protected Integer integerParam(HttpServletRequest request, String name) {
        return WebUtil.toInteger(request.getParameter(name));
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher(view).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirectWithMsg(HttpServletRequest request, HttpServletResponse response, String path,
                                   String message, boolean error) throws IOException {
        String key = error ? "err" : "msg";
        String url = request.getContextPath() + path + (path.contains("?") ? "&" : "?") + key + "="
                + URLEncoder.encode(message == null ? "" : message, StandardCharsets.UTF_8.name());
        response.sendRedirect(url);
    }

    protected int page(HttpServletRequest request) {
        int page = intParam(request, "page", 1);
        return page < 1 ? 1 : page;
    }

    protected int pageSize(HttpServletRequest request) {
        int size = intParam(request, "pageSize", 10);
        return size < 1 || size > 100 ? 10 : size;
    }

    protected int clampPage(int page, int total, int pageSize) {
        int totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        if (page > totalPages) {
            return totalPages;
        }
        return Math.max(page, 1);
    }

    protected void paginate(HttpServletRequest request, int page, int total, int pageSize) {
        int totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("total", total);
        request.setAttribute("totalPages", totalPages);
    }

    /**
     * 构造分页链接的查询串（排除 page / 提示参数），以 &amp; 开头。
     */
    protected String buildBaseQuery(HttpServletRequest request, String... excludes) {
        Set<String> skip = new HashSet<>(Arrays.asList("page", "msg", "err"));
        skip.addAll(Arrays.asList(excludes));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (skip.contains(key) || values == null || values.length == 0 || values[0].isEmpty()) {
                continue;
            }
            sb.append('&').append(key).append('=').append(encode(values[0]));
        }
        return sb.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "";
        }
    }
}
