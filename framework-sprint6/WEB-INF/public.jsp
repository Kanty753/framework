<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Page publique</title></head>
<body>
<h1>Page publique</h1>
<p><%= request.getAttribute("message") %></p>
<p>Cette page est accessible à tous, connecté ou non.</p>
<p><a href="${pageContext.request.contextPath}/app/login">Se connecter</a></p>
</body>
</html>

