<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<body>
<h2>Home</h2>

<c:choose>
    <c:when test="${not empty sessionScope.loginUser}">
        <div>안녕하세요, ${sessionScope.loginUser.name} (${sessionScope.loginUser.email})</div>
        <a href="${pageContext.request.contextPath}/user/logout">로그아웃</a>
    </c:when>
    <c:otherwise>
        <a href="${pageContext.request.contextPath}/user/login">로그인</a>
    </c:otherwise>
</c:choose>
</body>
</html>
