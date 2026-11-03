<%@ include file="/common/header.jsp" %>

<div class="stat-grid">
    <div class="stat-card income">
        <div class="icon">￥</div>
        <div class="label">今日收入</div>
        <div class="value">￥<fmt:formatNumber value="${todayIncome}" pattern="#,##0.00"/></div>
        <div class="hint">本月累计 ￥<fmt:formatNumber value="${monthIncome}" pattern="#,##0.00"/></div>
    </div>
    <div class="stat-card expense">
        <div class="icon">￥</div>
        <div class="label">今日支出</div>
        <div class="value">￥<fmt:formatNumber value="${todayExpense}" pattern="#,##0.00"/></div>
        <div class="hint">本月累计 ￥<fmt:formatNumber value="${monthExpense}" pattern="#,##0.00"/></div>
    </div>
    <div class="stat-card">
        <div class="icon">▤</div>
        <div class="label">在售商品</div>
        <div class="value">${goodsCount} <span style="font-size:13px;color:#6b7280">种</span></div>
        <div class="hint">库存总金额 ￥<fmt:formatNumber value="${stockValue}" pattern="#,##0.00"/></div>
    </div>
    <div class="stat-card">
        <div class="icon">!</div>
        <div class="label">库存预警</div>
        <div class="value" style="color:${lowStockCount > 0 ? '#d97706' : '#16a34a'}">${lowStockCount} <span
                style="font-size:13px;color:#6b7280">种</span></div>
        <div class="hint">待审核单据 ${pendingCount} 张</div>
    </div>
</div>

<div class="card">
    <div class="card-title">
        <h3>快捷操作 <span class="sub">按角色权限展示</span></h3>
    </div>
    <div class="quick-actions">
        <c:if test="${me.cashier}">
            <a class="btn btn-primary" href="${ctx}/ie?action=add&type=income">＋ 销售登记</a>
            <a class="btn btn-ghost" href="${ctx}/ie?action=list">我的流水</a>
            <a class="btn btn-ghost" href="${ctx}/stock?action=list">查询库存</a>
        </c:if>
        <c:if test="${me.manager}">
            <a class="btn btn-primary" href="${ctx}/ie?action=add&type=income">＋ 销售登记</a>
            <a class="btn btn-ghost" href="${ctx}/ie?action=add&type=expense">＋ 进货 / 杂费</a>
            <a class="btn btn-ghost" href="${ctx}/goods?action=add">新增商品</a>
            <a class="btn btn-ghost" href="${ctx}/stock?action=inOut">出入库</a>
            <a class="btn btn-ghost" href="${ctx}/report?action=index">查看报表</a>
        </c:if>
        <c:if test="${me.admin}">
            <a class="btn btn-primary" href="${ctx}/user?action=list">用户管理</a>
            <a class="btn btn-ghost" href="${ctx}/ie?action=list">全部流水</a>
            <a class="btn btn-ghost" href="${ctx}/report?action=index">报表中心</a>
            <a class="btn btn-ghost" href="${ctx}/setting">系统设置</a>
        </c:if>
    </div>
</div>

<div class="two-col">
    <div>
        <div class="card">
            <div class="card-title">
                <h3>最近收支流水</h3>
                <a href="${ctx}/ie?action=list" class="sub">查看全部 →</a>
            </div>
            <div class="table-wrap">
                <table class="table">
                    <thead>
                    <tr>
                        <th>单据编号</th>
                        <th>类型</th>
                        <th>类别</th>
                        <th>金额</th>
                        <th>经办人</th>
                        <th>状态</th>
                        <th>时间</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentList}" var="ie">
                        <tr>
                            <td class="text-muted">${ie.orderNo}</td>
                            <td>
                                <span class="tag ${ie.income ? 'success' : 'danger'}">${ie.typeName}</span>
                            </td>
                            <td>${ie.category}</td>
                            <td class="num ${ie.income ? 'money-in' : 'money-out'}">
                                ${ie.income ? '+' : '-'}￥<fmt:formatNumber value="${ie.money}" pattern="#,##0.00"/>
                            </td>
                            <td>${ie.userName}</td>
                            <td>
                                <span class="tag ${ie.status == 'approved' ? 'success' : (ie.status == 'rejected' ? 'danger' : 'warn')}">${ie.statusName}</span>
                            </td>
                            <td class="text-muted"><fmt:formatDate value="${ie.createTime}" pattern="MM-dd HH:mm"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recentList}">
                        <tr>
                            <td colspan="7" class="empty">暂无收支记录</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div>
        <div class="card">
            <div class="card-title">
                <h3>库存预警</h3>
                <c:if test="${me.supervisor}">
                    <a href="${ctx}/remind?action=list" class="sub">全部 →</a>
                </c:if>
            </div>
            <ul class="list-clean">
                <c:forEach items="${lowStockGoods}" var="g">
                    <li>
                        <span class="tag ${g.stockNum <= 0 ? 'danger' : 'warn'}">${g.stockNum <= 0 ? '缺货' : '偏低'}</span>
                        <span class="grow">${g.name}</span>
                        <span class="text-muted">${g.stockNum} / ${g.warnNum} ${g.unit}</span>
                    </li>
                </c:forEach>
                <c:if test="${empty lowStockGoods}">
                    <li class="text-muted">暂无低库存商品</li>
                </c:if>
            </ul>
        </div>

        <div class="card">
            <div class="card-title">
                <h3>待审核单据</h3>
                <c:if test="${me.supervisor}">
                    <a href="${ctx}/remind?action=list" class="sub">去审核 →</a>
                </c:if>
            </div>
            <ul class="list-clean">
                <c:forEach items="${pendingList}" var="p">
                    <li>
                        <span class="tag warn">待审</span>
                        <span class="grow">${p.goodsName} × ${p.quantity}</span>
                        <span class="money">￥<fmt:formatNumber value="${p.money}" pattern="#,##0.00"/></span>
                    </li>
                </c:forEach>
                <c:if test="${empty pendingList}">
                    <li class="text-muted">暂无待审核单据</li>
                </c:if>
            </ul>
        </div>
    </div>
</div>

<%@ include file="/common/footer.jsp" %>
