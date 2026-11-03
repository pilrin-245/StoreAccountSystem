package com.store.service;

import com.store.dao.UserDao;
import com.store.entity.User;
import com.store.util.MD5Util;

import java.util.List;

/**
 * 用户业务：登录、账号维护、密码修改。
 */
public class UserService {

    private final UserDao userDao = new UserDao();

    /**
     * 登录校验，失败返回 null；账号禁用抛出业务异常。
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null || !MD5Util.matches(password, user.getPassword())) {
            return null;
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        user.setPassword(null);
        return user;
    }

    public User findById(Integer id) {
        return userDao.findById(id);
    }

    public List<User> search(String keyword, String role, Integer status, int page, int pageSize) {
        return userDao.search(keyword, role, status, (page - 1) * pageSize, pageSize);
    }

    public int count(String keyword, String role, Integer status) {
        return userDao.count(keyword, role, status);
    }

    public void addUser(String username, String rawPassword, String role, String name, String phone) {
        if (username == null || username.trim().length() < 3) {
            throw new BusinessException("登录账号至少 3 个字符");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BusinessException("初始密码至少 6 位");
        }
        if (!"manager".equals(role) && !"cashier".equals(role)) {
            throw new BusinessException("只能新增店长或收银员账号");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("请填写姓名");
        }
        if (userDao.findByUsername(username.trim()) != null) {
            throw new BusinessException("登录账号已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(MD5Util.md5(rawPassword));
        user.setRole(role);
        user.setName(name.trim());
        user.setPhone(phone);
        user.setStatus(1);
        userDao.insert(user);
    }

    public void changeStatus(Integer id, int status) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.isAdmin()) {
            throw new BusinessException("管理员账号不允许禁用");
        }
        userDao.updateStatus(id, status == 1 ? 1 : 0);
    }

    public void resetPassword(Integer id, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BusinessException("新密码至少 6 位");
        }
        userDao.updatePassword(id, MD5Util.md5(rawPassword));
    }

    public User updateProfile(Integer id, String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        userDao.updateProfile(id, name.trim(), phone);
        User user = userDao.findById(id);
        user.setPassword(null);
        return user;
    }

    public void changePassword(Integer id, String oldPassword, String newPassword, String confirmPassword) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (!MD5Util.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少 6 位");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        userDao.updatePassword(id, MD5Util.md5(newPassword));
    }

    public int countByRole(String role) {
        return userDao.countByRole(role);
    }
}
