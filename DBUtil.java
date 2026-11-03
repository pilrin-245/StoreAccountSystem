package com.store.util;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库工具类：基于 Druid 连接池，并提供线程内事务支持。
 */
public final class DBUtil {

    private static final ThreadLocal<Connection> TRANSACTION = new ThreadLocal<>();
    private static final DataSource DATA_SOURCE = initDataSource();

    private DBUtil() {
    }

    private static DataSource initDataSource() {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("未找到 db.properties 配置文件");
            }
            Properties props = new Properties();
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));

            DruidDataSource ds = new DruidDataSource();
            ds.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            ds.setUrl(props.getProperty("db.url"));
            ds.setUsername(props.getProperty("db.username"));
            ds.setPassword(props.getProperty("db.password"));
            ds.setInitialSize(intProp(props, "db.initialSize", 2));
            ds.setMinIdle(intProp(props, "db.minIdle", 2));
            ds.setMaxActive(intProp(props, "db.maxActive", 10));
            ds.setMaxWait(intProp(props, "db.maxWait", 5000));
            ds.setValidationQuery(props.getProperty("db.validationQuery", "SELECT 1"));
            ds.setTestWhileIdle(true);
            ds.setTestOnBorrow(false);
            ds.setPoolPreparedStatements(false);
            ds.init();
            return ds;
        } catch (Exception e) {
            throw new ExceptionInInitializerError("数据库连接池初始化失败：" + e.getMessage());
        }
    }

    private static int intProp(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取连接：若当前线程处于事务中，则返回同一个连接。
     */
    public static Connection getConnection() throws SQLException {
        Connection txConnection = TRANSACTION.get();
        return txConnection != null ? txConnection : DATA_SOURCE.getConnection();
    }

    public static boolean inTransaction() {
        return TRANSACTION.get() != null;
    }

    /**
     * 关闭连接：事务连接由事务方法统一关闭。
     */
    public static void close(Connection conn) {
        if (conn == null || TRANSACTION.get() == conn) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
            // 忽略关闭异常
        }
    }

    /**
     * 在一个数据库事务中执行，出现异常自动回滚。
     */
    public static <T> T transaction(TxWork<T> work) {
        Connection existing = TRANSACTION.get();
        if (existing != null) {
            return work.run();
        }
        Connection conn = null;
        try {
            conn = DATA_SOURCE.getConnection();
            conn.setAutoCommit(false);
            TRANSACTION.set(conn);
            T result = work.run();
            conn.commit();
            return result;
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            TRANSACTION.remove();
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // ignore
                }
                close(conn);
            }
        }
    }

    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // ignore
            }
        }
    }

    @FunctionalInterface
    public interface TxWork<T> {
        T run();
    }
}
