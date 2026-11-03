package com.store.service;

import com.store.dao.GoodsDao;
import com.store.dao.UserDao;
import com.store.entity.Goods;
import com.store.entity.IncomeExpense;
import com.store.entity.User;
import com.store.util.DBUtil;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 核心业务集成测试：销售扣库存、库存不足拦截、驳回回滚、审核通过。
 * 依赖已初始化的 store_db 数据库与初始账号。
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessFlowTest {

    private static final GoodsDao GOODS_DAO = new GoodsDao();
    private static User cashier;
    private static User manager;
    private static Integer goodsId;

    @BeforeClass
    public static void setUp() {
        try {
            cashier = new UserDao().findByUsername("cashier");
            manager = new UserDao().findByUsername("manager");
        } catch (Exception e) {
            Assume.assumeTrue("数据库不可用，跳过集成测试：" + e.getMessage(), false);
        }
        Assume.assumeNotNull(cashier, manager);

        Goods goods = new Goods();
        goods.setName("JUnit 测试商品");
        goods.setCategory("测试");
        goods.setUnit("件");
        goods.setPrice(new BigDecimal("10.00"));
        goods.setCostPrice(new BigDecimal("5.00"));
        goods.setStockNum(20);
        goods.setWarnNum(5);
        goods.setRemark("单元测试临时数据");
        goods.setStatus(1);
        goodsId = GOODS_DAO.insert(goods);
        assertNotNull(goodsId);
    }

    @Test
    public void test01_saleDeductsStock() {
        IncomeExpense ie = new IEService().createSale(cashier, goodsId, 3, null, "JUnit 销售测试");
        assertEquals(17, GOODS_DAO.findById(goodsId).getStockNum().intValue());
        assertEquals("pending", ie.getStatus());
        assertEquals(new BigDecimal("30.00"), ie.getMoney());
    }

    @Test
    public void test02_insufficientStockBlocked() {
        int before = GOODS_DAO.findById(goodsId).getStockNum();
        try {
            new IEService().createSale(cashier, goodsId, 9999, null, "JUnit 超卖测试");
            fail("库存不足时应抛出业务异常");
        } catch (BusinessException expected) {
            assertEquals(before, GOODS_DAO.findById(goodsId).getStockNum().intValue());
        }
    }

    @Test
    public void test03_stockOutGuard() {
        int before = GOODS_DAO.findById(goodsId).getStockNum();
        try {
            new StockService().inOut(goodsId, "out", 9999, "JUnit 出库越界", manager.getId());
            fail("出库数量超过库存时应抛出业务异常");
        } catch (BusinessException expected) {
            assertEquals(before, GOODS_DAO.findById(goodsId).getStockNum().intValue());
        }
    }

    @Test
    public void test04_rejectRestoresStock() {
        int before = GOODS_DAO.findById(goodsId).getStockNum();
        IEService service = new IEService();
        IncomeExpense ie = service.createSale(cashier, goodsId, 2, null, "JUnit 驳回测试");
        assertEquals(before - 2, GOODS_DAO.findById(goodsId).getStockNum().intValue());

        service.reject(ie.getId(), manager, "JUnit 驳回");
        IncomeExpense rejected = new com.store.dao.IEDao().findById(ie.getId());
        assertEquals("rejected", rejected.getStatus());
        assertEquals(before, GOODS_DAO.findById(goodsId).getStockNum().intValue());
    }

    @Test
    public void test05_approvePending() {
        IEService service = new IEService();
        IncomeExpense ie = service.createSale(cashier, goodsId, 1, null, "JUnit 审核测试");
        service.approve(ie.getId(), manager);
        assertEquals("approved", new com.store.dao.IEDao().findById(ie.getId()).getStatus());
    }

    @AfterClass
    public static void tearDown() {
        if (goodsId == null) {
            return;
        }
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM t_stock_record WHERE goods_id = ?")) {
                ps.setInt(1, goodsId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM t_income_expense WHERE goods_id = ?")) {
                ps.setInt(1, goodsId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM t_goods WHERE id = ?")) {
                ps.setInt(1, goodsId);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
            // 清理失败不影响测试结果
        }
    }
}
