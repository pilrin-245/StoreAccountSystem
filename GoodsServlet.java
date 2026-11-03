package com.store.servlet;

import com.store.dao.ConfigDao;
import com.store.entity.Goods;
import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.GoodsService;
import com.store.util.TokenUtil;
import com.store.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品档案管理。
 */
@WebServlet("/goods")
public class GoodsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final GoodsService goodsService = new GoodsService();
    private final ConfigDao configDao = new ConfigDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        if ("add".equals(action)) {
            request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
            request.setAttribute("categories", goodsService.findCategories());
            request.setAttribute("defaultWarnNum", configDao.get("default_warn_num", "10"));
            request.setAttribute("pageTitle", "新增商品");
            forward(request, response, "/goods/goodsForm.jsp");
            return;
        }
        if ("edit".equals(action)) {
            Integer id = integerParam(request, "id");
            Goods goods = goodsService.findById(id);
            if (goods == null) {
                redirectWithMsg(request, response, "/goods?action=list", "商品不存在", true);
                return;
            }
            request.setAttribute("goods", goods);
            request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
            request.setAttribute("categories", goodsService.findCategories());
            request.setAttribute("pageTitle", "编辑商品");
            forward(request, response, "/goods/goodsForm.jsp");
            return;
        }
        list(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = param(request, "keyword");
        String category = param(request, "category");
        boolean lowStockOnly = "1".equals(param(request, "lowStock"));
        String statusText = param(request, "status");
        Integer status = statusText.isEmpty() ? null : Integer.valueOf(statusText);
        int page = page(request);
        int pageSize = pageSize(request);

        int total = goodsService.count(keyword, category, lowStockOnly, status);
        page = clampPage(page, total, pageSize);
        List<Goods> goodsList = goodsService.search(keyword, category, lowStockOnly, status, page, pageSize);

        request.setAttribute("goodsList", goodsList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("category", category);
        request.setAttribute("lowStock", lowStockOnly ? "1" : "");
        request.setAttribute("status", statusText);
        request.setAttribute("categories", goodsService.findCategories());
        request.setAttribute("baseQuery", buildBaseQuery(request));
        request.setAttribute("pageTitle", "商品档案");
        paginate(request, page, total, pageSize);
        forward(request, response, "/goods/goodsList.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = param(request, "action");
        User user = currentUser(request);
        try {
            if ("save".equals(action)) {
                if (!TokenUtil.verify(request)) {
                    redirectWithMsg(request, response, "/goods?action=list", "请勿重复提交表单", true);
                    return;
                }
                Goods goods = new Goods();
                goods.setName(param(request, "name"));
                goods.setCategory(param(request, "category", "其他"));
                goods.setUnit(param(request, "unit", "件"));
                goods.setPrice(WebUtil.toDecimal(param(request, "price")));
                goods.setCostPrice(WebUtil.toDecimal(param(request, "costPrice")));
                goods.setWarnNum(WebUtil.toInteger(param(request, "warnNum")));
                goods.setRemark(param(request, "remark"));
                int initStock = intParam(request, "initStock", 0);
                if (goods.getPrice() == null) {
                    throw new BusinessException("请填写正确的销售单价");
                }
                if (goods.getCostPrice() == null) {
                    goods.setCostPrice(BigDecimal.ZERO);
                }
                if (goods.getWarnNum() == null) {
                    goods.setWarnNum(10);
                }
                String idText = param(request, "id");
                if (idText.isEmpty()) {
                    goodsService.add(goods, initStock, user.getId());
                    redirectWithMsg(request, response, "/goods?action=list", "商品新增成功", false);
                } else {
                    goods.setId(Integer.valueOf(idText));
                    goods.setStatus(intParam(request, "status", 1));
                    goodsService.update(goods);
                    redirectWithMsg(request, response, "/goods?action=list", "商品修改成功", false);
                }
                return;
            }
            if ("toggle".equals(action)) {
                goodsService.toggleStatus(integerParam(request, "id"));
                redirectWithMsg(request, response, "/goods?action=list", "商品状态已更新", false);
                return;
            }
            if ("warn".equals(action)) {
                goodsService.updateWarnNum(integerParam(request, "id"), intParam(request, "warnNum", 10));
                redirectWithMsg(request, response, "/goods?action=list", "预警阈值已更新", false);
                return;
            }
            redirect(request, response, "/goods?action=list");
        } catch (BusinessException e) {
            String target = param(request, "id").isEmpty() ? "/goods?action=add" : "/goods?action=list";
            redirectWithMsg(request, response, target, e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/goods?action=list", "操作失败：" + e.getMessage(), true);
        }
    }
}
