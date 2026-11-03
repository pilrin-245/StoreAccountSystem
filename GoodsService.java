package com.store.service;

import com.store.dao.GoodsDao;
import com.store.entity.Goods;
import com.store.util.DBUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务：档案维护、库存查询。
 */
public class GoodsService {

    private final GoodsDao goodsDao = new GoodsDao();
    private final StockService stockService = new StockService();

    public Goods findById(Integer id) {
        return goodsDao.findById(id);
    }

    public List<Goods> search(String keyword, String category, boolean lowStockOnly, Integer status,
                              int page, int pageSize) {
        return goodsDao.search(keyword, category, lowStockOnly, status, (page - 1) * pageSize, pageSize);
    }

    public int count(String keyword, String category, boolean lowStockOnly, Integer status) {
        return goodsDao.count(keyword, category, lowStockOnly, status);
    }

    public List<Goods> findAllActive() {
        return goodsDao.findAllActive();
    }

    public List<String> findCategories() {
        return goodsDao.findCategories();
    }

    public List<Goods> findLowStock(int limit) {
        return goodsDao.findLowStock(limit);
    }

    public int countLowStock() {
        return goodsDao.countLowStock();
    }

    public int countGoods() {
        return goodsDao.countGoods();
    }

    public BigDecimal totalStockValue() {
        return goodsDao.totalStockValue();
    }

    public BigDecimal totalCostValue() {
        return goodsDao.totalCostValue();
    }

    /**
     * 新增商品；期初库存写入库存流水，保证台账可追溯。
     */
    public void add(Goods goods, int initStock, Integer operatorId) {
        validate(goods);
        if (initStock < 0) {
            throw new BusinessException("期初库存不能为负数");
        }
        DBUtil.transaction(() -> {
            Goods toSave = goods;
            toSave.setStockNum(0);
            int goodsId = goodsDao.insert(toSave);
            if (initStock > 0) {
                stockService.applyChange(goodsId, initStock, "in", "init", "新增商品期初库存", operatorId);
            }
            return goodsId;
        });
    }

    /**
     * 修改商品档案，不直接修改库存（库存必须走出入库）。
     */
    public void update(Goods goods) {
        if (goods.getId() == null) {
            throw new BusinessException("商品不存在");
        }
        validate(goods);
        Goods old = goodsDao.findById(goods.getId());
        if (old == null) {
            throw new BusinessException("商品不存在");
        }
        if (goodsDao.update(goods) == 0) {
            throw new BusinessException("商品更新失败");
        }
    }

    public void updateWarnNum(Integer id, int warnNum) {
        if (warnNum < 0) {
            throw new BusinessException("预警阈值不能为负数");
        }
        if (goodsDao.updateWarnNum(id, warnNum) == 0) {
            throw new BusinessException("商品不存在");
        }
    }

    public void toggleStatus(Integer id) {
        Goods goods = goodsDao.findById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        int newStatus = goods.getStatus() != null && goods.getStatus() == 1 ? 0 : 1;
        goods.setStatus(newStatus);
        goodsDao.update(goods);
    }

    private void validate(Goods goods) {
        if (goods.getName() == null || goods.getName().trim().isEmpty()) {
            throw new BusinessException("商品名称不能为空");
        }
        if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("销售单价不能为负数");
        }
        if (goods.getWarnNum() == null || goods.getWarnNum() < 0) {
            throw new BusinessException("预警阈值不能为负数");
        }
    }
}
