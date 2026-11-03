package com.store.servlet;

import com.store.dao.ConfigDao;
import com.store.entity.User;
import com.store.service.GoodsService;
import com.store.service.IEService;
import com.store.service.RemindService;
import com.store.util.DateUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * 首页仪表盘：营业概览 + 提醒卡片。
 */
@WebServlet({"/dashboard", "/index"})
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IEService ieService = new IEService();
    private final GoodsService goodsService = new GoodsService();
    private final RemindService remindService = new RemindService();
    private final ConfigDao configDao = new ConfigDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        Timestamp todayStart = DateUtil.parseStart(DateUtil.today());
        Timestamp todayEnd = DateUtil.parseEnd(DateUtil.today());
        Timestamp monthStart = DateUtil.startOfMonth(DateUtil.currentMonth());
        Timestamp monthEnd = DateUtil.endOfMonth(DateUtil.currentMonth());

        request.setAttribute("todayIncome", ieService.sumApproved("income", todayStart, todayEnd, user));
        request.setAttribute("todayExpense", ieService.sumApproved("expense", todayStart, todayEnd, user));
        request.setAttribute("monthIncome", ieService.sumApproved("income", monthStart, monthEnd, user));
        request.setAttribute("monthExpense", ieService.sumApproved("expense", monthStart, monthEnd, user));

        request.setAttribute("goodsCount", goodsService.countGoods());
        request.setAttribute("lowStockCount", remindService.lowStockCount());
        request.setAttribute("pendingCount", remindService.pendingCount());
        request.setAttribute("stockValue", goodsService.totalStockValue());

        request.setAttribute("lowStockGoods", remindService.lowStockGoods(5));
        request.setAttribute("pendingList", remindService.pendingRecords(5));
        request.setAttribute("recentList", ieService.recent(user, 8));
        request.setAttribute("storeName", configDao.get("store_name", "小店"));

        request.setAttribute("pageTitle", "首页");
        forward(request, response, "/home.jsp");
    }
}
