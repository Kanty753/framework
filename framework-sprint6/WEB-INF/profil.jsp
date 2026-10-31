<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Profil</title></head>
<body>
<h1>Profil utilisateur</h1>
<% if (request.getAttribute("message") != null) { %>
    <p style="color:green;"><%= request.getAttribute("message") %></p>
<% } %>
<p>Utilisateur : <strong><%= request.getAttribute("user") %></strong></p>
<p>Rôle : <strong><%= request.getAttribute("role") %></strong></p>
<form method="post" action="${pageContext.request.contextPath}/app/logout">
    <button type="submit">Se déconnecter</button>
</form>
</body>
</html>

