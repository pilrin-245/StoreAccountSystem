package com.store.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集行映射器。
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
