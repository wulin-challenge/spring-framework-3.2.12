<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>analysis springmvc security</title>
</head>
<body>
 <h2>This is SpringMVC demo page</h2>
 <c:forEach items="${users}" var="user">
 	<c:out value="${user.username}"/><br/>
 	<c:out value="${user.age}"/><br/>
 </c:forEach>
</body>
</html>