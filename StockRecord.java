package com.store.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 库存变动流水实体。
 */
public class StockRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer goodsId;
    private Integer changeNum;
    private String inOutType;
    private String sourceType;
    private String remark;
    private Timestamp operateTime;
    private Integer operatorId;

    /** 关联查询字段 */
    private String goodsName;
    private String goodsUnit;
    private String operatorName;

    public String getInOutName() {
        return "in".equals(inOutType) ? "入库" : "出库";
    }

    public String getSourceName() {
        if ("purchase".equals(sourceType)) {
            return "采购入库";
        }
        if ("sale".equals(sourceType)) {
            return "销售出库";
        }
        if ("reject".equals(sourceType)) {
            return "驳回回滚";
        }
        if ("init".equals(sourceType)) {
            return "期初库存";
        }
        return "手工调整";
    }

    public boolean isIn() {
        return "in".equals(inOutType);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getChangeNum() {
        return changeNum;
    }

    public void setChangeNum(Integer changeNum) {
        this.changeNum = changeNum;
    }

    public String getInOutType() {
        return inOutType;
    }

    public void setInOutType(String inOutType) {
        this.inOutType = inOutType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Timestamp getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Timestamp operateTime) {
        this.operateTime = operateTime;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsUnit() {
        return goodsUnit;
    }

    public void setGoodsUnit(String goodsUnit) {
        this.goodsUnit = goodsUnit;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
