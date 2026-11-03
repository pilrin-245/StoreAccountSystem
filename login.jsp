<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 店铺收支记账与库存管理系统</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body class="login-body">
<div class="login-card">
    <div class="logo">店</div>
    <h1>小型店铺收支记账系统</h1>
    <div class="subtitle">收支流水 · 商品库存 · 营业报表</div>

    <c:if test="${not empty error}">
        <div class="alert error"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty param.msg}">
        <div class="alert success"><c:out value="${param.msg}"/></div>
    </c:if>

    <form method="post" action="${ctx}/login" autocomplete="off">
        <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">
        <div class="field">
            <label for="username">登录账号</label>
            <input type="text" id="username" name="username" required
                   value="<c:out value='${username}'/>" placeholder="请输入账号">
        </div>
        <div class="field">
            <label for="password">登录密码</label>
            <input type="password" id="password" name="password" required placeholder="请输入密码">
        </div>
        <button type="submit" class="btn btn-primary">登 录</button>
    </form>

    <div class="login-tips">
        演示账号（密码均为 <code>123456</code>）：<br>
        管理员 <code>admin</code>　店长 <code>manager</code>　收银员 <code>cashier</code>
    </div>
</div>
</body>
</html>
