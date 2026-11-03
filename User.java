package com.store.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户实体：管理员 / 店长 / 收银员。
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String password;
    private String role;
    private String name;
    private String phone;
    private Integer status;
    private Timestamp createTime;

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isManager() {
        return "manager".equals(role);
    }

    public boolean isCashier() {
        return "cashier".equals(role);
    }

    /**
     * 是否为管理岗（管理员或店长），用于页面按钮控制。
     */
    public boolean isSupervisor() {
        return isAdmin() || isManager();
    }

    public String getRoleName() {
        if (isAdmin()) {
            return "管理员";
        }
        if (isManager()) {
            return "店长";
        }
        if (isCashier()) {
            return "收银员";
        }
        return role;
    }

    public String getStatusText() {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
