package com.store.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

/**
 * 商品实体。
 */
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String category;
    private String unit;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockNum;
    private Integer warnNum;
    private String remark;
    private Integer status;
    private Timestamp createTime;

    public boolean isLowStock() {
        int stock = stockNum == null ? 0 : stockNum;
        int warn = warnNum == null ? 0 : warnNum;
        return stock <= warn;
    }

    public BigDecimal getStockValue() {
        BigDecimal p = price == null ? BigDecimal.ZERO : price;
        int stock = stockNum == null ? 0 : stockNum;
        return p.multiply(BigDecimal.valueOf(stock)).setScale(2, RoundingMode.HALF_UP);
    }

    public String getStatusText() {
        return status != null && status == 1 ? "在售" : "停用";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getStockNum() {
        return stockNum;
    }

    public void setStockNum(Integer stockNum) {
        this.stockNum = stockNum;
    }

    public Integer getWarnNum() {
        return warnNum;
    }

    public void setWarnNum(Integer warnNum) {
        this.warnNum = warnNum;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
