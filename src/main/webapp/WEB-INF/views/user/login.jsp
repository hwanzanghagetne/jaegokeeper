<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Login</title>
</head>
<body>

<h2>로그인</h2>

<c:if test="${not empty error}">
    <div style="color:red;">${error}</div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/user/login">
    <div>
        <label>Email</label>
        <input type="email" name="email" required value="${param.email}" />
    </div>

    <div>
        <label>Password</label>
        <input type="password" name="password" required />
    </div>

    <button type="submit">로그인</button>
</form>

</body>
</html>
