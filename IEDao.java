package com.store.dao;

import com.store.entity.IncomeExpense;
import com.store.entity.StatRow;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 收支单据数据访问层。
 */
public class IEDao extends BaseDao {

    private static final String SELECT_JOIN =
            "SELECT ie.id, ie.order_no, ie.type, ie.category, ie.money, ie.remark, ie.goods_id, ie.quantity, "
                    + "ie.user_id, ie.status, ie.audit_user_id, ie.audit_time, ie.create_time, "
                    + "u.name AS user_name, g.name AS goods_name, au.name AS audit_user_name "
                    + "FROM t_income_expense ie "
                    + "LEFT JOIN t_user u ON ie.user_id = u.id "
                    + "LEFT JOIN t_goods g ON ie.goods_id = g.id "
                    + "LEFT JOIN t_user au ON ie.audit_user_id = au.id ";

    private IncomeExpense map(ResultSet rs) throws SQLException {
        IncomeExpense ie = new IncomeExpense();
        ie.setId(rs.getInt("id"));
        ie.setOrderNo(rs.getString("order_no"));
        ie.setType(rs.getString("type"));
        ie.setCategory(rs.getString("category"));
        ie.setMoney(rs.getBigDecimal("money"));
        ie.setRemark(rs.getString("remark"));
        ie.setGoodsId((Integer) rs.getObject("goods_id"));
        ie.setQuantity((Integer) rs.getObject("quantity"));
        ie.setUserId(rs.getInt("user_id"));
        ie.setStatus(rs.getString("status"));
        ie.setAuditUserId((Integer) rs.getObject("audit_user_id"));
        ie.setAuditTime(rs.getTimestamp("audit_time"));
        ie.setCreateTime(rs.getTimestamp("create_time"));
        ie.setUserName(rs.getString("user_name"));
        ie.setGoodsName(rs.getString("goods_name"));
        ie.setAuditUserName(rs.getString("audit_user_name"));
        return ie;
    }

    public int insert(IncomeExpense ie) {
        return insert("INSERT INTO t_income_expense (order_no, type, category, money, remark, goods_id, quantity, "
                        + "user_id, status, audit_user_id, audit_time) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                ie.getOrderNo(), ie.getType(), ie.getCategory(), ie.getMoney(), ie.getRemark(), ie.getGoodsId(),
                ie.getQuantity(), ie.getUserId(), ie.getStatus(), ie.getAuditUserId(), ie.getAuditTime());
    }

    public IncomeExpense findById(Integer id) {
        return queryOne(SELECT_JOIN + "WHERE ie.id = ?", this::map, id);
    }

    public List<IncomeExpense> search(String type, String status, String keyword, Timestamp start, Timestamp end,
                                      Integer userId, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String sql = SELECT_JOIN + buildWhere(type, status, keyword, start, end, userId, args)
                + " ORDER BY ie.create_time DESC, ie.id DESC LIMIT ? OFFSET ?";
        args.add(limit);
        args.add(offset);
        return queryList(sql, this::map, args.toArray());
    }

    public int count(String type, String status, String keyword, Timestamp start, Timestamp end, Integer userId) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM t_income_expense ie LEFT JOIN t_goods g ON ie.goods_id = g.id "
                + buildWhere(type, status, keyword, start, end, userId, args);
        return queryInt(sql, args.toArray());
    }

    private String buildWhere(String type, String status, String keyword, Timestamp start, Timestamp end,
                              Integer userId, List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (type != null && !type.isEmpty()) {
            where.append(" AND ie.type = ?");
            args.add(type);
        }
        if (status != null && !status.isEmpty()) {
            where.append(" AND ie.status = ?");
            args.add(status);
        }
        if (userId != null) {
            where.append(" AND ie.user_id = ?");
            args.add(userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND (ie.order_no LIKE ? OR ie.remark LIKE ? OR g.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (start != null) {
            where.append(" AND ie.create_time >= ?");
            args.add(start);
        }
        if (end != null) {
            where.append(" AND ie.create_time <= ?");
            args.add(end);
        }
        return where.toString();
    }

    public List<IncomeExpense> recent(Integer userId, int limit) {
        List<Object> args = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE 1=1";
        if (userId != null) {
            sql += " AND ie.user_id = ?";
            args.add(userId);
        }
        sql += " ORDER BY ie.create_time DESC, ie.id DESC LIMIT ?";
        args.add(limit);
        return queryList(sql, this::map, args.toArray());
    }

    public List<IncomeExpense> listPending(int limit) {
        return queryList(SELECT_JOIN + "WHERE ie.status = 'pending' ORDER BY ie.create_time ASC LIMIT ?",
                this::map, limit);
    }

    public int countPending() {
        return queryInt("SELECT COUNT(*) FROM t_income_expense WHERE status = 'pending'");
    }

    public int updateStatus(Integer id, String status, Integer auditUserId, Timestamp auditTime) {
        return update("UPDATE t_income_expense SET status = ?, audit_user_id = ?, audit_time = ? WHERE id = ?",
                status, auditUserId, auditTime, id);
    }

    /**
     * 汇总收入或支出（仅统计已通过单据）。
     */
    public BigDecimal sumApproved(String type, Timestamp start, Timestamp end) {
        return queryDecimal("SELECT COALESCE(SUM(money),0) FROM t_income_expense "
                        + "WHERE type = ? AND status = 'approved' AND create_time BETWEEN ? AND ?",
                type, start, end);
    }

    public BigDecimal sumApprovedByUser(String type, Timestamp start, Timestamp end, Integer userId) {
        return queryDecimal("SELECT COALESCE(SUM(money),0) FROM t_income_expense "
                        + "WHERE type = ? AND status = 'approved' AND create_time BETWEEN ? AND ? AND user_id = ?",
                type, start, end, userId);
    }

    public List<StatRow> dailyStats(Timestamp start, Timestamp end) {
        return queryList("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS label, "
                        + "COALESCE(SUM(CASE WHEN type='income' THEN money ELSE 0 END),0) AS income, "
                        + "COALESCE(SUM(CASE WHEN type='expense' THEN money ELSE 0 END),0) AS expense, "
                        + "COUNT(*) AS order_count "
                        + "FROM t_income_expense WHERE status='approved' AND create_time BETWEEN ? AND ? "
                        + "GROUP BY label ORDER BY label DESC",
                this::mapStat, start, end);
    }

    public List<StatRow> monthlyStats(Timestamp start, Timestamp end) {
        return queryList("SELECT DATE_FORMAT(create_time, '%Y-%m') AS label, "
                        + "COALESCE(SUM(CASE WHEN type='income' THEN money ELSE 0 END),0) AS income, "
                        + "COALESCE(SUM(CASE WHEN type='expense' THEN money ELSE 0 END),0) AS expense, "
                        + "COUNT(*) AS order_count "
                        + "FROM t_income_expense WHERE status='approved' AND create_time BETWEEN ? AND ? "
                        + "GROUP BY label ORDER BY label DESC",
                this::mapStat, start, end);
    }

    public List<StatRow> goodsSalesStats(Timestamp start, Timestamp end) {
        return queryList("SELECT g.name AS label, COALESCE(SUM(ie.quantity),0) AS quantity, "
                        + "COALESCE(SUM(ie.money),0) AS income, COUNT(*) AS order_count "
                        + "FROM t_income_expense ie JOIN t_goods g ON ie.goods_id = g.id "
                        + "WHERE ie.type='income' AND ie.status='approved' AND ie.goods_id IS NOT NULL "
                        + "AND ie.create_time BETWEEN ? AND ? "
                        + "GROUP BY g.id, g.name ORDER BY quantity DESC, income DESC",
                rs -> {
                    StatRow row = new StatRow();
                    row.setLabel(rs.getString("label"));
                    row.setQuantity(rs.getInt("quantity"));
                    row.setIncome(rs.getBigDecimal("income"));
                    row.setOrderCount(rs.getInt("order_count"));
                    return row;
                }, start, end);
    }

    private StatRow mapStat(ResultSet rs) throws SQLException {
        StatRow row = new StatRow();
        row.setLabel(rs.getString("label"));
        row.setIncome(rs.getBigDecimal("income"));
        row.setExpense(rs.getBigDecimal("expense"));
        row.setOrderCount(rs.getInt("order_count"));
        return row;
    }
}
