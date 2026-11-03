package com.store.service;

import com.store.dao.GoodsDao;
import com.store.dao.StockDao;
import com.store.entity.Goods;
import com.store.entity.StockRecord;
import com.store.util.DBUtil;

import java.util.List;

/**
 * 库存业务：出入库处理、库存查询、流水查询。
 */
public class StockService {

    private final StockDao stockDao = new StockDao();
    private final GoodsDao goodsDao = new GoodsDao();

    public List<Goods> searchGoods(String keyword, String category, boolean lowStockOnly, int page, int pageSize) {
        return goodsDao.search(keyword, category, lowStockOnly, null, (page - 1) * pageSize, pageSize);
    }

    public int countGoods(String keyword, String category, boolean lowStockOnly) {
        return goodsDao.count(keyword, category, lowStockOnly, null);
    }

    public List<StockRecord> searchRecords(Integer goodsId, String inOutType, String keyword,
                                           java.sql.Timestamp start, java.sql.Timestamp end,
                                           int page, int pageSize) {
        return stockDao.search(goodsId, inOutType, keyword, start, end, (page - 1) * pageSize, pageSize);
    }

    public int countRecords(Integer goodsId, String inOutType, String keyword,
                            java.sql.Timestamp start, java.sql.Timestamp end) {
        return stockDao.count(goodsId, inOutType, keyword, start, end);
    }

    public List<StockRecord> recentRecords(int limit) {
        return stockDao.recent(limit);
    }

    /**
     * 手工出/入库。
     */
    public void inOut(Integer goodsId, String inOutType, int changeNum, String remark, Integer operatorId) {
        if (goodsId == null) {
            throw new BusinessException("请选择商品");
        }
        if (changeNum <= 0) {
            throw new BusinessException("变动数量必须大于 0");
        }
        if (!"in".equals(inOutType) && !"out".equals(inOutType)) {
            throw new BusinessException("入库类型不合法");
        }
        applyChange(goodsId, changeNum, inOutType, "adjust", remark, operatorId);
    }

    /**
     * 库存变动核心方法：行锁读取 → 校验 → 更新库存 → 写流水，整体在一个事务内完成。
     */
    public void applyChange(Integer goodsId, int changeNum, String inOutType, String sourceType,
                            String remark, Integer operatorId) {
        if (changeNum <= 0) {
            throw new BusinessException("库存变动数量必须大于 0");
        }
        DBUtil.transaction(() -> {
            Integer current = goodsDao.lockStock(goodsId);
            if (current == null) {
                throw new BusinessException("商品不存在");
            }
            int newStock = "in".equals(inOutType) ? current + changeNum : current - changeNum;
            if (newStock < 0) {
                throw new BusinessException("库存不足：当前库存 " + current + "，本次需要出库 " + changeNum);
            }
            goodsDao.updateStock(goodsId, newStock);

            StockRecord record = new StockRecord();
            record.setGoodsId(goodsId);
            record.setChangeNum(changeNum);
            record.setInOutType(inOutType);
            record.setSourceType(sourceType);
            record.setRemark(remark);
            record.setOperatorId(operatorId);
            stockDao.insert(record);
            return newStock;
        });
    }
}
