<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Administration</title></head>
<body>
<h1>Panneau d'administration</h1>
<p>Bienvenue, administrateur <strong><%= request.getAttribute("user") %></strong> !</p>
<p>Cette page est réservée aux utilisateurs ayant le rôle <em>admin</em>.</p>
<form method="post" action="${pageContext.request.contextPath}/app/logout">
    <button type="submit">Se déconnecter</button>
</form>
</body>
</html>

