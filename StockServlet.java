package com.store.servlet;

import com.store.entity.Goods;
import com.store.entity.StockRecord;
import com.store.entity.User;
import com.store.service.BusinessException;
import com.store.service.GoodsService;
import com.store.service.StockService;
import com.store.util.DateUtil;
import com.store.util.TokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 库存管理：库存查询、手工出入库、库存流水。
 */
@WebServlet("/stock")
public class StockServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final StockService stockService = new StockService();
    private final GoodsService goodsService = new GoodsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        if ("inOut".equals(action)) {
            request.setAttribute("goodsList", goodsService.findAllActive());
            request.setAttribute("formToken", TokenUtil.generate(request.getSession()));
            request.setAttribute("selectedGoodsId", integerParam(request, "goodsId"));
            request.setAttribute("defaultType", param(request, "type", "in"));
            request.setAttribute("pageTitle", "出入库操作");
            forward(request, response, "/stock/stockInOut.jsp");
            return;
        }
        if ("records".equals(action)) {
            records(request, response);
            return;
        }
        list(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = param(request, "keyword");
        String category = param(request, "category");
        boolean lowStockOnly = "1".equals(param(request, "lowStock"));
        int page = page(request);
        int pageSize = pageSize(request);

        int total = stockService.countGoods(keyword, category, lowStockOnly);
        page = clampPage(page, total, pageSize);
        List<Goods> goodsList = stockService.searchGoods(keyword, category, lowStockOnly, page, pageSize);

        request.setAttribute("goodsList", goodsList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("category", category);
        request.setAttribute("lowStock", lowStockOnly ? "1" : "");
        request.setAttribute("categories", goodsService.findCategories());
        request.setAttribute("baseQuery", buildBaseQuery(request));
        request.setAttribute("pageTitle", "库存查询");
        paginate(request, page, total, pageSize);
        forward(request, response, "/stock/stockList.jsp");
    }

    private void records(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer goodsId = integerParam(request, "goodsId");
        String inOutType = param(request, "inOutType");
        String keyword = param(request, "keyword");
        String startDate = param(request, "startDate");
        String endDate = param(request, "endDate");
        int page = page(request);
        int pageSize = pageSize(request);

        java.sql.Timestamp start = DateUtil.parseStart(startDate);
        java.sql.Timestamp end = DateUtil.parseEnd(endDate);
        int total = stockService.countRecords(goodsId, inOutType, keyword, start, end);
        page = clampPage(page, total, pageSize);
        List<StockRecord> records = stockService.searchRecords(goodsId, inOutType, keyword, start, end, page, pageSize);

        request.setAttribute("records", records);
        request.setAttribute("goodsId", goodsId);
        request.setAttribute("inOutType", inOutType);
        request.setAttribute("keyword", keyword);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("goodsList", goodsService.findAllActive());
        request.setAttribute("baseQuery", buildBaseQuery(request));
        request.setAttribute("pageTitle", "库存流水");
        paginate(request, page, total, pageSize);
        forward(request, response, "/stock/recordList.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        String action = param(request, "action");
        if (!"save".equals(action)) {
            redirect(request, response, "/stock?action=list");
            return;
        }
        try {
            if (!TokenUtil.verify(request)) {
                redirectWithMsg(request, response, "/stock?action=inOut", "请勿重复提交表单", true);
                return;
            }
            Integer goodsId = integerParam(request, "goodsId");
            String inOutType = param(request, "inOutType", "in");
            int changeNum = intParam(request, "changeNum", 0);
            String remark = param(request, "remark");
            stockService.inOut(goodsId, inOutType, changeNum, remark, user.getId());
            redirectWithMsg(request, response, "/stock?action=records",
                    "库存" + ("in".equals(inOutType) ? "入库" : "出库") + "成功", false);
        } catch (BusinessException e) {
            redirectWithMsg(request, response, "/stock?action=inOut", e.getMessage(), true);
        } catch (RuntimeException e) {
            redirectWithMsg(request, response, "/stock?action=list", "操作失败：" + e.getMessage(), true);
        }
    }
}
