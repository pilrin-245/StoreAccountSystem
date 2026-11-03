package com.store.service;

import com.store.dao.GoodsDao;
import com.store.dao.IEDao;
import com.store.entity.Goods;
import com.store.entity.IncomeExpense;
import com.store.entity.User;
import com.store.util.DBUtil;
import com.store.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 收支业务：销售/进货/杂费登记、审核、查询与汇总。
 */
public class IEService {

    private final IEDao ieDao = new IEDao();
    private final GoodsDao goodsDao = new GoodsDao();
    private final StockService stockService = new StockService();

    public List<IncomeExpense> search(String type, String status, String keyword, String startDate, String endDate,
                                      int page, int pageSize, User viewer) {
        return ieDao.search(type, status, keyword, DateUtil.parseStart(startDate), DateUtil.parseEnd(endDate),
                scopeUserId(viewer), (page - 1) * pageSize, pageSize);
    }

    public int count(String type, String status, String keyword, String startDate, String endDate, User viewer) {
        return ieDao.count(type, status, keyword, DateUtil.parseStart(startDate), DateUtil.parseEnd(endDate),
                scopeUserId(viewer));
    }

    public IncomeExpense findById(Integer id, User viewer) {
        IncomeExpense ie = ieDao.findById(id);
        if (ie == null) {
            throw new BusinessException("单据不存在");
        }
        if (viewer.isCashier() && !viewer.getId().equals(ie.getUserId())) {
            throw new BusinessException("无权查看他人的单据");
        }
        return ie;
    }

    public List<IncomeExpense> recent(User viewer, int limit) {
        return ieDao.recent(scopeUserId(viewer), limit);
    }

    public List<IncomeExpense> pendingRecords(int limit) {
        return ieDao.listPending(limit);
    }

    public int pendingCount() {
        return ieDao.countPending();
    }

    /**
     * 收银员只能看到自己的单据，管理岗可以看全部。
     */
    private Integer scopeUserId(User viewer) {
        return viewer != null && viewer.isCashier() ? viewer.getId() : null;
    }

    /**
     * 新增销售收入：立即扣减库存；收银员提交后为待审核，管理岗提交直接通过。
     */
    public IncomeExpense createSale(User operator, Integer goodsId, int quantity, BigDecimal money, String remark) {
        if (goodsId == null) {
            throw new BusinessException("请选择商品");
        }
        if (quantity <= 0) {
            throw new BusinessException("销售数量必须大于 0");
        }
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            throw new BusinessException("商品不存在或已停用");
        }
        BigDecimal amount = money;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = goods.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("销售金额必须大于 0");
        }

        boolean supervisor = operator.isSupervisor();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        IncomeExpense ie = new IncomeExpense();
        ie.setOrderNo(nextOrderNo("SO"));
        ie.setType("income");
        ie.setCategory("销售");
        ie.setMoney(amount);
        ie.setRemark(remark);
        ie.setGoodsId(goodsId);
        ie.setQuantity(quantity);
        ie.setUserId(operator.getId());
        ie.setStatus(supervisor ? "approved" : "pending");
        ie.setAuditUserId(supervisor ? operator.getId() : null);
        ie.setAuditTime(supervisor ? now : null);

        DBUtil.transaction(() -> {
            int id = ieDao.insert(ie);
            stockService.applyChange(goodsId, quantity, "out", "sale",
                    "销售出库：" + ie.getOrderNo(), operator.getId());
            ie.setId(id);
            return id;
        });
        ie.setGoodsName(goods.getName());
        ie.setUserName(operator.getName());
        return ie;
    }

    /**
     * 进货支出：仅管理岗可操作，入库并直接审核通过。
     */
    public IncomeExpense createPurchase(User operator, Integer goodsId, int quantity, BigDecimal money, String remark) {
        ensureSupervisor(operator);
        if (goodsId == null) {
            throw new BusinessException("请选择进货商品");
        }
        if (quantity <= 0) {
            throw new BusinessException("进货数量必须大于 0");
        }
        if (money == null || money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("进货金额必须大于 0");
        }
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        IncomeExpense ie = new IncomeExpense();
        ie.setOrderNo(nextOrderNo("PO"));
        ie.setType("expense");
        ie.setCategory("进货");
        ie.setMoney(money);
        ie.setRemark(remark);
        ie.setGoodsId(goodsId);
        ie.setQuantity(quantity);
        ie.setUserId(operator.getId());
        ie.setStatus("approved");
        ie.setAuditUserId(operator.getId());
        ie.setAuditTime(now);

        DBUtil.transaction(() -> {
            int id = ieDao.insert(ie);
            stockService.applyChange(goodsId, quantity, "in", "purchase",
                    "进货入库：" + ie.getOrderNo(), operator.getId());
            ie.setId(id);
            return id;
        });
        ie.setGoodsName(goods.getName());
        ie.setUserName(operator.getName());
        return ie;
    }

    /**
     * 杂费支出：不涉及库存。
     */
    public IncomeExpense createExpense(User operator, BigDecimal money, String remark) {
        ensureSupervisor(operator);
        if (money == null || money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支出金额必须大于 0");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        IncomeExpense ie = new IncomeExpense();
        ie.setOrderNo(nextOrderNo("EX"));
        ie.setType("expense");
        ie.setCategory("杂费");
        ie.setMoney(money);
        ie.setRemark(remark);
        ie.setUserId(operator.getId());
        ie.setStatus("approved");
        ie.setAuditUserId(operator.getId());
        ie.setAuditTime(now);
        int id = ieDao.insert(ie);
        ie.setId(id);
        ie.setUserName(operator.getName());
        return ie;
    }

    /**
     * 审核通过：库存已在销售时扣减，无需再次变动。
     */
    public void approve(Integer id, User auditor) {
        ensureSupervisor(auditor);
        IncomeExpense ie = ieDao.findById(id);
        if (ie == null) {
            throw new BusinessException("单据不存在");
        }
        if (!ie.isPending()) {
            throw new BusinessException("该单据已处理，请勿重复审核");
        }
        ieDao.updateStatus(id, "approved", auditor.getId(), new Timestamp(System.currentTimeMillis()));
    }

    /**
     * 审核驳回：回滚销售扣减的库存。
     */
    public void reject(Integer id, User auditor, String reason) {
        ensureSupervisor(auditor);
        IncomeExpense ie = ieDao.findById(id);
        if (ie == null) {
            throw new BusinessException("单据不存在");
        }
        if (!ie.isPending()) {
            throw new BusinessException("该单据已处理，请勿重复审核");
        }
        String auditRemark = reason == null || reason.trim().isEmpty() ? "审核驳回" : "审核驳回：" + reason.trim();
        DBUtil.transaction(() -> {
            if (ie.getGoodsId() != null && ie.getQuantity() != null && ie.getQuantity() > 0) {
                if ("income".equals(ie.getType())) {
                    stockService.applyChange(ie.getGoodsId(), ie.getQuantity(), "in", "reject",
                            auditRemark + "（退回库存）", auditor.getId());
                } else {
                    stockService.applyChange(ie.getGoodsId(), ie.getQuantity(), "out", "reject",
                            auditRemark + "（冲减库存）", auditor.getId());
                }
            }
            ieDao.updateStatus(id, "rejected", auditor.getId(), new Timestamp(System.currentTimeMillis()));
            return id;
        });
    }

    private void ensureSupervisor(User user) {
        if (user == null || !user.isSupervisor()) {
            throw new BusinessException("当前角色无权执行该操作");
        }
    }

    /**
     * 已通过单据金额汇总（收银员仅统计本人）。
     */
    public BigDecimal sumApproved(String type, Timestamp start, Timestamp end, User viewer) {
        return viewer != null && viewer.isCashier()
                ? ieDao.sumApprovedByUser(type, start, end, viewer.getId())
                : ieDao.sumApproved(type, start, end);
    }

    private String nextOrderNo(String prefix) {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + time + random;
    }
}
