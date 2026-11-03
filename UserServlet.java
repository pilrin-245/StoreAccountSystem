package com.store.servlet;

import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 用户管理（仅管理员）。
 */
@WebServlet("/user")
public class UserServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        if ("add".equals(action)) {
            request.setAttribute("pageTitle", "新增账号");
            forward(request, response, "/user/userForm.jsp");
            return;
        }
        list(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = param(request, "keyword");
        String role = param(request, "role");
        String statusText = param(request, "status");
        Integer status = statusText.isEmpty() ? null : Integer.valueOf(statusText);
        int page = page(request);
        int pageSize = pageSize(request);

        int total = userService.count(keyword, role, status);
        page = clampPage(page, total, pageSize);
        List<User> users = userService.search(keyword, role, status, page, pageSize);

        request.setAttribute("users", users);
        request.setAttribute("keyword", keyword);
        request.setAttribute("role", role);
        request.setAttribute("status", statusText);
        request.setAttribute("baseQuery", buildBaseQuery(request));
        request.setAttribute("pageTitle", "用户管理");
        paginate(request, page, total, pageSize);
        forward(request, response, "/user/userList.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = param(request, "action");
        try {
            if ("save".equals(action)) {
                if (!com.store.util.TokenUtil.verify(request)) {
                    redirectWithMsg(request, response, "/user?action=add", "请勿重复提交表单", true);
                    return;
                }
                userService.addUser(param(request, "username"), request.getParameter("password"),
                        param(request, "role"), param(request, "name"), param(request, "phone"));
                redirectWithMsg(request, response, "/user?action=list", "账号新增成功", false);
                return;
            }
            if ("toggle".equals(action)) {
                Integer id = integerParam(request, "id");
                int status = intParam(request, "status", 0);
                userService.changeStatus(id, status);
                redirectWithMsg(request, response, "/user?action=list",
                        status == 1 ? "账号已启用" : "账号已禁用", false);
                return;
            }
            if ("resetPwd".equals(action)) {
                Integer id = integerParam(request, "id");
                userService.resetPassword(id, "123456");
                redirectWithMsg(request, response, "/user?action=list", "密码已重置为 123456", false);
                return;
            }
            redirect(request, response, "/user?action=list");
        } catch (BusinessException e) {
            redirectWithMsg(request, response, "/user?action=list", e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/user?action=list", "操作失败：" + e.getMessage(), true);
        }
    }
}
