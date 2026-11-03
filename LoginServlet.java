package com.store.servlet;

import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录 / 退出。
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request) != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        forward(request, response, "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = param(request, "username");
        String password = request.getParameter("password");
        try {
            User user = userService.login(username, password);
            if (user == null) {
                request.setAttribute("error", "账号或密码错误");
                request.setAttribute("username", username);
                forward(request, response, "/login.jsp");
                return;
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("loginUser", user);
            session.setAttribute("loginTime", new java.util.Date());
            session.setMaxInactiveInterval(30 * 60);

            String redirect = param(request, "redirect");
            if (!redirect.isEmpty() && redirect.startsWith("/")) {
                response.sendRedirect(request.getContextPath() + redirect);
            } else {
                redirect(request, response, "/dashboard");
            }
        } catch (BusinessException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("username", username);
            forward(request, response, "/login.jsp");
        } catch (ExceptionInInitializerError e) {
            request.setAttribute("error", "数据库连接失败，请检查 db.properties 配置");
            forward(request, response, "/login.jsp");
        } catch (RuntimeException e) {
            request.setAttribute("error", "登录异常：" + e.getMessage());
            forward(request, response, "/login.jsp");
        }
    }
}
