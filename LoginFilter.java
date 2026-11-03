package com.store.filter;

import com.store.entity.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截 + 角色权限校验过滤器。
 */
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());

        // 静态资源与登录页直接放行
        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loginUser");
        if (user == null) {
            String target = request.getQueryString() == null ? path : path + "?" + request.getQueryString();
            response.sendRedirect(contextPath + "/login?redirect="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8.name()));
            return;
        }

        // 禁止绕过 Servlet 直接访问内部 JSP 页面
        if (path.endsWith(".jsp") && !"/index.jsp".equals(path)) {
            response.sendRedirect(contextPath + "/dashboard");
            return;
        }

        if (!allowed(user, path, request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("denyMessage", "当前角色无权访问该功能");
            request.getRequestDispatcher("/error/403.jsp").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return path.isEmpty()
                || "/".equals(path)
                || "/index.jsp".equals(path)
                || "/login".equals(path)
                || "/login.jsp".equals(path)
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/error/")
                || "/favicon.ico".equals(path);
    }

    private boolean allowed(User user, String path, HttpServletRequest request) {
        String action = request.getParameter("action") == null ? "" : request.getParameter("action");

        if (path.startsWith("/user")) {
            // 用户管理：仅管理员
            return user.isAdmin();
        }
        if (path.startsWith("/setting")) {
            return user.isAdmin();
        }
        if (path.startsWith("/goods")) {
            // 收银员只能查询商品，不能维护档案
            if (user.isCashier()) {
                return action.isEmpty() || "list".equals(action);
            }
            return true;
        }
        if (path.startsWith("/ie")) {
            if ("approve".equals(action) || "reject".equals(action)) {
                return user.isSupervisor();
            }
            return true;
        }
        if (path.startsWith("/stock")) {
            if ("inOut".equals(action) || "save".equals(action)) {
                return user.isSupervisor();
            }
            return true;
        }
        if (path.startsWith("/report")) {
            // 收银员不开放报表
            return user.isSupervisor();
        }
        if (path.startsWith("/remind")) {
            return user.isSupervisor();
        }
        return true;
    }

    @Override
    public void destroy() {
        // nothing
    }
}
