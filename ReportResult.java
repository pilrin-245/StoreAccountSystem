package com.store.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表结果封装：统计行 + 合计。
 */
public class ReportResult {

    private List<StatRow> rows = new ArrayList<>();
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private int totalQuantity;
    private int totalOrders;

    public List<StatRow> getRows() {
        return rows;
    }

    public void setRows(List<StatRow> rows) {
        this.rows = rows;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
}
