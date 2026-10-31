<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Espace RH</title></head>
<body>
<h1>Espace Ressources Humaines</h1>
<p>Bienvenue, <strong><%= request.getAttribute("user") %></strong>
   (rôle : <em><%= request.getAttribute("role") %></em>) !</p>
<p>Cette page est réservée aux utilisateurs ayant le rôle <em>admin</em> ou <em>rh</em>.</p>
<form method="post" action="${pageContext.request.contextPath}/app/logout">
    <button type="submit">Se déconnecter</button>
</form>
</body>
</html>

