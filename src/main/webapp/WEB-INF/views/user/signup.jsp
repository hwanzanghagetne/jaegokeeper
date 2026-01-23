<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Signup</title>
</head>
<body>

<h2>회원가입</h2>

<c:if test="${not empty error}">
    <div style="color:red;">${error}</div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/user/signup">
    <div>
        <label>Email</label>
        <input type="email" name="email" required value="${param.email}" />
    </div>

    <div>
        <label>Name</label>
        <input type="text" name="name" required value="${param.name}" />
    </div>

    <div>
        <label>Password</label>
        <input type="password" name="password" required />
    </div>

    <div>
        <label>Password Confirm</label>
        <input type="password" name="passwordConfirm" required />
    </div>

    <button type="submit">가입</button>
</form>

<div style="margin-top:12px;">
    <a href="${pageContext.request.contextPath}/user/login">로그인으로</a>
</div>

</body>
</html>
