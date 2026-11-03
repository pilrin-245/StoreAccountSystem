package com.store.servlet;

import com.store.dao.ConfigDao;
import com.store.service.BusinessException;
import com.store.util.TokenUtil;
import com.store.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 系统基础配置（仅管理员）。
 */
@WebServlet("/setting")
public class SettingServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final ConfigDao configDao = new ConfigDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("config", configDao.findAll());
        request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
        request.setAttribute("pageTitle", "系统设置");
        forward(request, response, "/setting/setting.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!TokenUtil.verify(request)) {
            redirectWithMsg(request, response, "/setting", "请勿重复提交表单", true);
            return;
        }
        try {
            String storeName = param(request, "storeName");
            int defaultWarn = WebUtil.toInt(param(request, "defaultWarnNum"), 10);
            if (storeName.isEmpty()) {
                throw new BusinessException("店铺名称不能为空");
            }
            if (defaultWarn < 0 || defaultWarn > 99999) {
                throw new BusinessException("默认预警阈值不合理");
            }
            configDao.save("store_name", storeName, "店铺名称，显示在系统顶部与报表抬头");
            configDao.save("default_warn_num", String.valueOf(defaultWarn), "新增商品时默认的库存预警阈值");
            redirectWithMsg(request, response, "/setting", "系统配置已保存", false);
        } catch (BusinessException e) {
            redirectWithMsg(request, response, "/setting", e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/setting", "保存失败：" + e.getMessage(), true);
        }
    }
}
