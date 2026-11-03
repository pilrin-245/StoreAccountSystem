package com.store.servlet;

import com.store.service.RemindService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 提醒中心：低库存预警 + 待审核单据。
 */
@WebServlet("/remind")
public class RemindServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final RemindService remindService = new RemindService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("lowStockGoods", remindService.lowStockGoods(100));
        request.setAttribute("pendingList", remindService.pendingRecords(100));
        request.setAttribute("lowStockCount", remindService.lowStockCount());
        request.setAttribute("pendingCount", remindService.pendingCount());
        request.setAttribute("pageTitle", "提醒中心");
        forward(request, response, "/remind/remindList.jsp");
    }
}
