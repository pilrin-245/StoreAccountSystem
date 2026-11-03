<%@ include file="/common/header.jsp" %>

<div class="two-col">
    <div>
        <div class="card">
            <div class="card-title">
                <h3>个人资料</h3>
                <span class="role-badge">${user.roleName}</span>
            </div>
            <form method="post" action="${ctx}/profile">
                <input type="hidden" name="action" value="updateProfile">
                <input type="hidden" name="_token" value="${formToken}">
                <div class="form-grid">
                    <div class="field">
                        <label>登录账号</label>
                        <input type="text" value="${user.username}" disabled>
                    </div>
                    <div class="field">
                        <label>角色</label>
                        <input type="text" value="${user.roleName}" disabled>
                    </div>
                    <div class="field">
                        <label>姓名 *</label>
                        <input type="text" name="name" required maxlength="50" value="<c:out value='${user.name}'/>">
                    </div>
                    <div class="field">
                        <label>手机号</label>
                        <input type="text" name="phone" maxlength="20" value="<c:out value='${user.phone}'/>">
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">保存资料</button>
                </div>
            </form>
        </div>

        <c:if test="${me.cashier}">
            <div class="card">
                <div class="card-title">
                    <h3>我最近的流水</h3>
                    <a class="sub" href="${ctx}/ie?action=list">查看全部 →</a>
                </div>
                <div class="table-wrap">
                    <table class="table">
                        <thead>
                        <tr>
                            <th>单据编号</th>
                            <th>类别</th>
                            <th>金额</th>
                            <th>状态</th>
                            <th>时间</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${myRecords}" var="ie">
                            <tr>
                                <td class="text-muted">${ie.orderNo}</td>
                                <td>${ie.category}</td>
                                <td class="num ${ie.income ? 'money-in' : 'money-out'}">
                                    ${ie.income ? '+' : '-'}￥<fmt:formatNumber value="${ie.money}" pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <span class="tag ${ie.status == 'approved' ? 'success' : (ie.status == 'rejected' ? 'danger' : 'warn')}">${ie.statusName}</span>
                                </td>
                                <td class="text-muted"><fmt:formatDate value="${ie.createTime}" pattern="MM-dd HH:mm"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty myRecords}">
                            <tr>
                                <td colspan="5" class="empty">暂无流水记录</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </div>

    <div>
        <div class="card">
            <div class="card-title">
                <h3>修改密码</h3>
            </div>
            <form method="post" action="${ctx}/profile" autocomplete="off">
                <input type="hidden" name="action" value="changePassword">
                <input type="hidden" name="_token" value="${formToken}">
                <div class="field" style="margin-bottom:14px">
                    <label>原密码 *</label>
                    <input type="password" name="oldPassword" required>
                </div>
                <div class="field" style="margin-bottom:14px">
                    <label>新密码 *</label>
                    <input type="password" name="newPassword" required minlength="6">
                </div>
                <div class="field" style="margin-bottom:14px">
                    <label>确认新密码 *</label>
                    <input type="password" name="confirmPassword" required minlength="6">
                </div>
                <div class="form-actions" style="margin-top:0">
                    <button type="submit" class="btn btn-primary">修改密码</button>
                </div>
                <p class="hint-text" style="margin-top:12px">修改成功后需要重新登录。</p>
            </form>
        </div>

        <div class="card">
            <div class="card-title">
                <h3>账号信息</h3>
            </div>
            <ul class="list-clean">
                <li><span class="text-muted" style="width:80px">登录账号</span><span class="grow">${user.username}</span></li>
                <li><span class="text-muted" style="width:80px">角色权限</span><span class="grow">${user.roleName}</span></li>
                <li><span class="text-muted" style="width:80px">账号状态</span><span class="grow">${user.statusText}</span></li>
                <li><span class="text-muted" style="width:80px">创建时间</span><span class="grow"><fmt:formatDate
                        value="${user.createTime}" pattern="yyyy-MM-dd HH:mm"/></span></li>
            </ul>
        </div>
    </div>
</div>

<%@ include file="/common/footer.jsp" %>
