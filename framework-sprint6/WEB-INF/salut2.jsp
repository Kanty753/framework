<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="app.models.Personne" %>
<%
    Personne p = (Personne) request.getAttribute("p");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Salut2 — objet simple</title>
</head>
<body>
    <h1>Bonjour, <%= p != null ? p.getNom() : "(inconnu)" %> !</h1>
    <% if (p != null) { %>
        <p>Âge : <%= p.getAge() %> ans</p>
        <% if (p.getAdresse() != null) { %>
            <p>Adresse : <%= p.getAdresse().getRue() %>, <%= p.getAdresse().getVille() %></p>
        <% } %>
    <% } %>
    <a href="../form2.html">Retour au formulaire</a>
</body>
</html>
