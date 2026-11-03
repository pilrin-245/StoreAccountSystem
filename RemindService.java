package com.store.service;

import com.store.dao.IEDao;
import com.store.dao.GoodsDao;
import com.store.entity.Goods;
import com.store.entity.IncomeExpense;

import java.util.List;

/**
 * 提醒业务：低库存预警 + 待审核单据提醒。
 */
public class RemindService {

    private final GoodsDao goodsDao = new GoodsDao();
    private final IEDao ieDao = new IEDao();

    public List<Goods> lowStockGoods(int limit) {
        return goodsDao.findLowStock(limit);
    }

    public List<IncomeExpense> pendingRecords(int limit) {
        return ieDao.listPending(limit);
    }

    public int lowStockCount() {
        return goodsDao.countLowStock();
    }

    public int pendingCount() {
        return ieDao.countPending();
    }

    /**
     * 是否有需要提醒的事项。
     */
    public boolean hasReminder() {
        return lowStockCount() > 0 || pendingCount() > 0;
    }
}
