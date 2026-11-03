package com.store.dao;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统配置数据访问层。
 */
public class ConfigDao extends BaseDao {

    public String get(String key, String defaultValue) {
        String value = queryOne("SELECT config_value FROM t_config WHERE config_key = ?",
                rs -> rs.getString(1), key);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    public Map<String, String> findAll() {
        Map<String, String> map = new LinkedHashMap<>();
        queryList("SELECT config_key, config_value FROM t_config ORDER BY config_key", rs -> {
            map.put(rs.getString(1), rs.getString(2));
            return null;
        });
        return map;
    }

    public int save(String key, String value, String remark) {
        return update("INSERT INTO t_config (config_key, config_value, remark) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)", key, value, remark);
    }
}
