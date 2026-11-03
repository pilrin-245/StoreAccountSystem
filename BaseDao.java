package com.store.dao;

import com.store.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO 基类：封装 JDBC 模板代码，自动适配事务连接。
 */
public abstract class BaseDao {

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapper.map(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn);
        }
    }

    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    protected int queryInt(String sql, Object... params) {
        Integer value = queryOne(sql, rs -> rs.getInt(1), params);
        return value == null ? 0 : value;
    }

    protected BigDecimal queryDecimal(String sql, Object... params) {
        BigDecimal value = queryOne(sql, rs -> rs.getBigDecimal(1), params);
        return value == null ? BigDecimal.ZERO : value;
    }

    protected int update(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, params);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("更新失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * 插入并返回自增主键。
     */
    protected int insert(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(ps, params);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("新增失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn);
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            if (value == null) {
                ps.setNull(i + 1, java.sql.Types.NULL);
            } else if (value instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) value);
            } else if (value instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    /**
     * 生成 IN 占位符，如 3 个参数返回 "?,?,?"。
     */
    protected String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }
}
