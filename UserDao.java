package com.store.dao;

import com.store.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问层。
 */
public class UserDao extends BaseDao {

    private static final String COLUMNS = "id, username, password, role, name, phone, status, create_time";

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setName(rs.getString("name"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        return user;
    }

    public User findByUsername(String username) {
        return queryOne("SELECT " + COLUMNS + " FROM t_user WHERE username = ?", this::map, username);
    }

    public User findById(Integer id) {
        return queryOne("SELECT " + COLUMNS + " FROM t_user WHERE id = ?", this::map, id);
    }

    public List<User> search(String keyword, String role, Integer status, int offset, int limit) {
        List<Object> args = new ArrayList<>();
        String sql = buildSearchSql(keyword, role, status, args) + " ORDER BY id ASC LIMIT ? OFFSET ?";
        args.add(limit);
        args.add(offset);
        return queryList(sql, this::map, args.toArray());
    }

    public int count(String keyword, String role, Integer status) {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM t_user" + buildWhere(keyword, role, status, args);
        return queryInt(sql, args.toArray());
    }

    private String buildSearchSql(String keyword, String role, Integer status, List<Object> args) {
        return "SELECT " + COLUMNS + " FROM t_user" + buildWhere(keyword, role, status, args);
    }

    private String buildWhere(String keyword, String role, Integer status, List<Object> args) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND (username LIKE ? OR name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (role != null && !role.trim().isEmpty()) {
            where.append(" AND role = ?");
            args.add(role.trim());
        }
        if (status != null) {
            where.append(" AND status = ?");
            args.add(status);
        }
        return where.toString();
    }

    public int insert(User user) {
        return insert("INSERT INTO t_user (username, password, role, name, phone, status) VALUES (?,?,?,?,?,?)",
                user.getUsername(), user.getPassword(), user.getRole(), user.getName(), user.getPhone(),
                user.getStatus() == null ? 1 : user.getStatus());
    }

    public int updateStatus(Integer id, int status) {
        return update("UPDATE t_user SET status = ? WHERE id = ?", status, id);
    }

    public int updatePassword(Integer id, String encryptedPassword) {
        return update("UPDATE t_user SET password = ? WHERE id = ?", encryptedPassword, id);
    }

    public int updateProfile(Integer id, String name, String phone) {
        return update("UPDATE t_user SET name = ?, phone = ? WHERE id = ?", name, phone, id);
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM t_user");
    }

    public int countByRole(String role) {
        return queryInt("SELECT COUNT(*) FROM t_user WHERE role = ? AND status = 1", role);
    }
}
