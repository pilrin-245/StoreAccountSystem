package com.store.dao;

import com.store.entity.Goods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据访问层。
 */
public class GoodsDao extends BaseDao {

    private static final String COLUMNS =
            "id, name, category, unit, price, cost_price, stock_num, warn_num, remark, status, create_time";

    private Goods map(ResultSet rs) throws SQLException {
        Goods goods = new Goods();
        goods.setId(rs.getInt("id"));
        goods.setName(rs.getString("name"));
        goods.setCategory(rs.getString("category"));
        goods.setUnit(rs.getString("unit"));
        goods.setPrice(rs.getBigDecimal("price"));
        goods.setCostPrice(rs.getBigDecimal("cost_price"));
        goods.setStockNum(rs.getInt("stock_num"));
        goods.setWarnNum(rs.getInt("warn_num"));
        goods.setRemark(rs.getString("remark"));
        goods.setStatus(rs.getInt("status"));
        goods.setCreateTime(rs.getTimestamp("create_time"));
        return goods;
    }

    public Goods findById(Integer id) {
        return queryOne("SELECT " + COLUMNS + " FROM t_goods WHERE id = ?", this::map, id);
    }

    public List<Goods> search(String keyword, String category, boolean lowStockOnly, Integer status,
                              int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String sql = buildSearchSql(keyword, category, lowStockOnly, status, args) + " ORDER BY id DESC LIMIT ? OFFSET ?";
        args.add(limit);
        args.add(offset);
        return queryList(sql, this::map, args.toArray());
    }

    public int count(String keyword, String category, boolean lowStockOnly, Integer status) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM t_goods" + buildWhere(keyword, category, lowStockOnly, status, args);
        return queryInt(sql, args.toArray());
    }

    private String buildSearchSql(String keyword, String category, boolean lowStockOnly, Integer status,
                                  List<Object> args) {
        return "SELECT " + COLUMNS + " FROM t_goods" + buildWhere(keyword, category, lowStockOnly, status, args);
    }

    private String buildWhere(String keyword, String category, boolean lowStockOnly, Integer status,
                              List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND (name LIKE ? OR category LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (category != null && !category.trim().isEmpty()) {
            where.append(" AND category = ?");
            args.add(category.trim());
        }
        if (lowStockOnly) {
            where.append(" AND stock_num <= warn_num");
        }
        if (status != null) {
            where.append(" AND status = ?");
            args.add(status);
        }
        return where.toString();
    }

    /**
     * 所有在售商品，供下拉选择。
     */
    public List<Goods> findAllActive() {
        return queryList("SELECT " + COLUMNS + " FROM t_goods WHERE status = 1 ORDER BY name ASC", this::map);
    }

    public List<Goods> findLowStock(int limit) {
        return queryList("SELECT " + COLUMNS + " FROM t_goods WHERE status = 1 AND stock_num <= warn_num "
                + "ORDER BY (stock_num - warn_num) ASC LIMIT ?", this::map, limit);
    }

    public List<String> findCategories() {
        return queryList("SELECT DISTINCT category FROM t_goods WHERE category IS NOT NULL ORDER BY category",
                rs -> rs.getString(1));
    }

    public int insert(Goods goods) {
        return insert("INSERT INTO t_goods (name, category, unit, price, cost_price, stock_num, warn_num, remark, status) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                goods.getName(), goods.getCategory(), goods.getUnit(), goods.getPrice(), goods.getCostPrice(),
                goods.getStockNum() == null ? 0 : goods.getStockNum(),
                goods.getWarnNum() == null ? 10 : goods.getWarnNum(),
                goods.getRemark(), goods.getStatus() == null ? 1 : goods.getStatus());
    }

    public int update(Goods goods) {
        return update("UPDATE t_goods SET name=?, category=?, unit=?, price=?, cost_price=?, warn_num=?, remark=?, status=? "
                        + "WHERE id=?",
                goods.getName(), goods.getCategory(), goods.getUnit(), goods.getPrice(), goods.getCostPrice(),
                goods.getWarnNum(), goods.getRemark(), goods.getStatus(), goods.getId());
    }

    public int updateStock(Integer id, int stockNum) {
        return update("UPDATE t_goods SET stock_num = ? WHERE id = ?", stockNum, id);
    }

    public int updateWarnNum(Integer id, int warnNum) {
        return update("UPDATE t_goods SET warn_num = ? WHERE id = ?", warnNum, id);
    }

    /**
     * 行锁读取库存，必须在事务中调用。
     */
    public Integer lockStock(Integer id) {
        return queryOne("SELECT stock_num FROM t_goods WHERE id = ? FOR UPDATE", rs -> rs.getInt(1), id);
    }

    public int countLowStock() {
        return queryInt("SELECT COUNT(*) FROM t_goods WHERE status = 1 AND stock_num <= warn_num");
    }

    public int countGoods() {
        return queryInt("SELECT COUNT(*) FROM t_goods WHERE status = 1");
    }

    public java.math.BigDecimal totalStockValue() {
        return queryDecimal("SELECT COALESCE(SUM(price * stock_num), 0) FROM t_goods WHERE status = 1");
    }

    public java.math.BigDecimal totalCostValue() {
        return queryDecimal("SELECT COALESCE(SUM(cost_price * stock_num), 0) FROM t_goods WHERE status = 1");
    }
}
