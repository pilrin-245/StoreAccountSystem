package com.store.servlet;

import com.store.entity.IncomeExpense;
import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.IEService;
import com.store.service.UserService;
import com.store.util.TokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * 个人中心：资料维护与密码修改。
 */
@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserService();
    private final IEService ieService = new IEService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = currentUser(request);
        User user = userService.findById(loginUser.getId());
        user.setPassword(null);
        request.setAttribute("user", user);
        request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
        if (user.isCashier()) {
            List<IncomeExpense> myRecords = ieService.recent(user, 10);
            request.setAttribute("myRecords", myRecords);
        }
        request.setAttribute("pageTitle", "个人中心");
        forward(request, response, "/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = param(request, "action");
        try {
            if (!TokenUtil.verify(request)) {
                redirectWithMsg(request, response, "/profile", "请勿重复提交表单", true);
                return;
            }
            User loginUser = currentUser(request);
            if ("updateProfile".equals(action)) {
                User updated = userService.updateProfile(loginUser.getId(), param(request, "name"),
                        param(request, "phone"));
                HttpSession session = request.getSession();
                session.setAttribute("loginUser", updated);
                redirectWithMsg(request, response, "/profile", "个人资料已更新", false);
                return;
            }
            if ("changePassword".equals(action)) {
                userService.changePassword(loginUser.getId(), request.getParameter("oldPassword"),
                        request.getParameter("newPassword"), request.getParameter("confirmPassword"));
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                redirectWithMsg(request, response, "/login", "密码修改成功，请重新登录", false);
                return;
            }
            redirect(request, response, "/profile");
        } catch (BusinessException e) {
            redirectWithMsg(request, response, "/profile", e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/profile", "操作失败：" + e.getMessage(), true);
        }
    }
}
