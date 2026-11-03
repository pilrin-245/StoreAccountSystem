package com.store.servlet;

import com.store.entity.IncomeExpense;
import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.GoodsService;
import com.store.service.IEService;
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
 * 收支管理：登记销售/进货/杂费，查询流水，审核单据。
 */
@WebServlet("/ie")
public class IEServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IEService ieService = new IEService();
    private final GoodsService goodsService = new GoodsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        if ("add".equals(action)) {
            request.setAttribute("goodsList", goodsService.findAllActive());
            request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
            request.setAttribute("defaultType", param(request, "type", "income"));
            request.setAttribute("pageTitle", "新增收支");
            forward(request, response, "/ie/ieForm.jsp");
            return;
        }
        list(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String type = param(request, "type");
        String status = param(request, "status");
        String keyword = param(request, "keyword");
        String startDate = param(request, "startDate");
        String endDate = param(request, "endDate");
        int page = page(request);
        int pageSize = pageSize(request);

        int total = ieService.count(type, status, keyword, startDate, endDate, user);
        page = clampPage(page, total, pageSize);
        List<IncomeExpense> records = ieService.search(type, status, keyword, startDate, endDate, page, pageSize, user);

        request.setAttribute("records", records);
        request.setAttribute("type", type);
        request.setAttribute("status", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("baseQuery", buildBaseQuery(request));
        request.setAttribute("pageTitle", "收支管理");
        paginate(request, page, total, pageSize);
        forward(request, response, "/ie/ieList.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = param(request, "action");
        User user = currentUser(request);
        try {
            if ("save".equals(action)) {
                if (!TokenUtil.verify(request)) {
                    redirectWithMsg(request, response, "/ie?action=add", "请勿重复提交表单", true);
                    return;
                }
                String type = param(request, "type", "income");
                Integer goodsId = integerParam(request, "goodsId");
                int quantity = intParam(request, "quantity", 0);
                BigDecimal money = WebUtil.toDecimal(param(request, "money"));
                String remark = param(request, "remark");

                IncomeExpense created;
                if ("income".equals(type)) {
                    created = ieService.createSale(user, goodsId, quantity, money, remark);
                } else if ("进货".equals(param(request, "category"))) {
                    created = ieService.createPurchase(user, goodsId, quantity, money, remark);
                } else {
                    created = ieService.createExpense(user, money, remark);
                }
                String message = "单据 " + created.getOrderNo() + " 登记成功"
                        + ("pending".equals(created.getStatus()) ? "，等待店长审核" : "");
                redirectWithMsg(request, response, "/ie?action=list", message, false);
                return;
            }
            if ("approve".equals(action)) {
                ieService.approve(integerParam(request, "id"), user);
                redirectWithMsg(request, response, backUrl(request), "单据已审核通过", false);
                return;
            }
            if ("reject".equals(action)) {
                ieService.reject(integerParam(request, "id"), user, param(request, "reason"));
                redirectWithMsg(request, response, backUrl(request), "单据已驳回，库存已回滚", false);
                return;
            }
            redirect(request, response, "/ie?action=list");
        } catch (BusinessException e) {
            redirectWithMsg(request, response, backUrl(request), e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/ie?action=list", "操作失败：" + e.getMessage(), true);
        }
    }

    private String backUrl(HttpServletRequest request) {
        return "remind".equals(param(request, "from")) ? "/remind?action=list" : "/ie?action=list";
    }
}
