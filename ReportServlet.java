package com.store.servlet;

import com.store.entity.Goods;
import com.store.entity.ReportResult;
import com.store.entity.StatRow;
import com.store.service.ReportService;
import com.store.util.DateUtil;
import com.store.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 报表中心：日/月营收、商品销量、库存台账、CSV 导出。
 */
@WebServlet("/report")
public class ReportServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "index");
        if ("export".equals(action)) {
            export(request, response);
            return;
        }

        String reportType = action;
        if ("index".equals(reportType) || reportType.isEmpty()) {
            reportType = "daily";
        }
        String startDate = param(request, "startDate");
        String endDate = param(request, "endDate");
        String month = param(request, "month");
        String keyword = param(request, "keyword");
        boolean lowStockOnly = "1".equals(param(request, "lowStock"));

        request.setAttribute("pageTitle", "报表中心");
        request.setAttribute("reportType", reportType);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("month", month);
        request.setAttribute("keyword", keyword);
        request.setAttribute("lowStock", lowStockOnly ? "1" : "");
        request.setAttribute("baseQuery", buildBaseQuery(request));

        try {
            if ("monthly".equals(reportType)) {
                ReportResult report = reportService.monthlyReport(month);
                request.setAttribute("report", report);
            } else if ("goods".equals(reportType)) {
                ReportResult report = reportService.goodsSalesReport(startDate, endDate);
                request.setAttribute("report", report);
            } else if ("ledger".equals(reportType)) {
                request.setAttribute("ledger", reportService.stockLedger(keyword, lowStockOnly));
                request.setAttribute("reportType", "ledger");
            } else {
                ReportResult report = reportService.dailyReport(startDate, endDate);
                request.setAttribute("report", report);
                request.setAttribute("reportType", "daily");
            }
        } catch (RuntimeException e) {
            request.setAttribute("error", e.getMessage());
        }

        forward(request, response, "/report/report.jsp");
    }

    private void export(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = param(request, "type", "daily");
        String startDate = param(request, "startDate");
        String endDate = param(request, "endDate");
        String month = param(request, "month");
        String keyword = param(request, "keyword");
        boolean lowStockOnly = "1".equals(param(request, "lowStock"));

        String fileName = "report_" + type + "_" + DateUtil.today() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');

        if ("ledger".equals(type)) {
            writer.println("商品名称,分类,单位,当前库存,预警阈值,销售单价,库存金额,状态");
            for (Goods goods : reportService.stockLedger(keyword, lowStockOnly)) {
                writer.println(String.join(",",
                        WebUtil.csv(goods.getName()), WebUtil.csv(goods.getCategory()), WebUtil.csv(goods.getUnit()),
                        WebUtil.csv(goods.getStockNum()), WebUtil.csv(goods.getWarnNum()),
                        WebUtil.csv(goods.getPrice()), WebUtil.csv(goods.getStockValue()),
                        WebUtil.csv(goods.getStatusText())));
            }
            writer.flush();
            return;
        }

        ReportResult report;
        if ("monthly".equals(type)) {
            writer.println("月份,收入金额,支出金额,利润,单据数");
            report = reportService.monthlyReport(month);
            for (StatRow row : report.getRows()) {
                writer.println(String.join(",", WebUtil.csv(row.getLabel()), WebUtil.csv(row.getIncome()),
                        WebUtil.csv(row.getExpense()), WebUtil.csv(row.getProfit()),
                        WebUtil.csv(row.getOrderCount())));
            }
        } else if ("goods".equals(type)) {
            writer.println("商品名称,销售数量,销售金额,单据数");
            report = reportService.goodsSalesReport(startDate, endDate);
            for (StatRow row : report.getRows()) {
                writer.println(String.join(",", WebUtil.csv(row.getLabel()), WebUtil.csv(row.getQuantity()),
                        WebUtil.csv(row.getIncome()), WebUtil.csv(row.getOrderCount())));
            }
        } else {
            writer.println("日期,收入金额,支出金额,利润,单据数");
            report = reportService.dailyReport(startDate, endDate);
            for (StatRow row : report.getRows()) {
                writer.println(String.join(",", WebUtil.csv(row.getLabel()), WebUtil.csv(row.getIncome()),
                        WebUtil.csv(row.getExpense()), WebUtil.csv(row.getProfit()),
                        WebUtil.csv(row.getOrderCount())));
            }
        }
        writer.println();
        writer.println("合计," + WebUtil.csv(report.getTotalIncome()) + "," + WebUtil.csv(report.getTotalExpense())
                + "," + WebUtil.csv(report.getTotalProfit()) + "," + WebUtil.csv(report.getTotalOrders()));
        writer.flush();
    }
}
