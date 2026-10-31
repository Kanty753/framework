<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head><title>Connexion</title></head>
<body>
<h1>Connexion</h1>
<%-- Affichage des messages d'erreur ou de déconnexion --%>
<% if (request.getAttribute("erreur") != null) { %>
    <p style="color:red;"><%= request.getAttribute("erreur") %></p>
<% } %>
<% if (request.getAttribute("message") != null) { %>
    <p style="color:green;"><%= request.getAttribute("message") %></p>
<% } %>
<form method="post" action="${pageContext.request.contextPath}/app/login">
    <label>Utilisateur : <input type="text" name="username" /></label><br/>
    <label>Mot de passe : <input type="password" name="password" /></label><br/>
    <button type="submit">Se connecter</button>
</form>
<p><em>Comptes de test : admin/admin123 et user/user123</em></p>
</body>
</html>

