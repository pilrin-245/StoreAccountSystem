package com.store.service;

import com.store.dao.GoodsDao;
import com.store.dao.IEDao;
import com.store.entity.Goods;
import com.store.entity.ReportResult;
import com.store.entity.StatRow;
import com.store.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 报表业务：日/月营收、商品销量、库存台账。
 */
public class ReportService {

    private final IEDao ieDao = new IEDao();
    private final GoodsDao goodsDao = new GoodsDao();

    /**
     * 日报表：默认最近 7 天。
     */
    public ReportResult dailyReport(String startDate, String endDate) {
        if (endDate == null || endDate.isEmpty()) {
            endDate = DateUtil.today();
        }
        if (startDate == null || startDate.isEmpty()) {
            startDate = DateUtil.formatDate(DateUtil.daysAgo(6));
        }
        Timestamp start = DateUtil.parseStart(startDate);
        Timestamp end = DateUtil.parseEnd(endDate);
        if (start == null || end == null) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd");
        }
        return build(ieDao.dailyStats(start, end));
    }

    /**
     * 月报表：默认最近 6 个月。
     */
    public ReportResult monthlyReport(String endMonth) {
        if (endMonth == null || endMonth.trim().isEmpty()) {
            endMonth = DateUtil.currentMonth();
        }
        Timestamp end = DateUtil.endOfMonth(endMonth);
        if (end == null) {
            throw new BusinessException("月份格式不正确，应为 yyyy-MM");
        }
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(DateUtil.startOfMonth(endMonth));
        calendar.add(java.util.Calendar.MONTH, -5);
        Timestamp start = new Timestamp(calendar.getTimeInMillis());
        return build(ieDao.monthlyStats(start, end));
    }

    /**
     * 商品销量报表。
     */
    public ReportResult goodsSalesReport(String startDate, String endDate) {
        if (endDate == null || endDate.isEmpty()) {
            endDate = DateUtil.today();
        }
        if (startDate == null || startDate.isEmpty()) {
            String month = DateUtil.currentMonth() + "-01";
            startDate = month;
        }
        Timestamp start = DateUtil.parseStart(startDate);
        Timestamp end = DateUtil.parseEnd(endDate);
        if (start == null || end == null) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd");
        }
        ReportResult result = build(ieDao.goodsSalesStats(start, end));
        int quantity = 0;
        for (StatRow row : result.getRows()) {
            quantity += row.getQuantity();
        }
        result.setTotalQuantity(quantity);
        return result;
    }

    /**
     * 库存台账：全部商品及库存金额。
     */
    public List<Goods> stockLedger(String keyword, boolean lowStockOnly) {
        return goodsDao.search(keyword, null, lowStockOnly, null, 0, 1000);
    }

    private ReportResult build(List<StatRow> rows) {
        ReportResult result = new ReportResult();
        result.setRows(rows);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        int orders = 0;
        for (StatRow row : rows) {
            income = income.add(row.getIncome());
            expense = expense.add(row.getExpense());
            orders += row.getOrderCount();
        }
        result.setTotalIncome(income);
        result.setTotalExpense(expense);
        result.setTotalProfit(income.subtract(expense));
        result.setTotalOrders(orders);
        return result;
    }
}
