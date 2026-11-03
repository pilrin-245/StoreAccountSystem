package com.store.entity;

import java.math.BigDecimal;

/**
 * 报表统计行：日/月营收、商品销量通用。
 */
public class StatRow {

    private String label;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private Integer quantity = 0;
    private Integer orderCount = 0;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getIncome() {
        return income == null ? BigDecimal.ZERO : income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpense() {
        return expense == null ? BigDecimal.ZERO : expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    public BigDecimal getProfit() {
        return getIncome().subtract(getExpense());
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public Integer getQuantity() {
        return quantity == null ? 0 : quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getOrderCount() {
        return orderCount == null ? 0 : orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }
}
