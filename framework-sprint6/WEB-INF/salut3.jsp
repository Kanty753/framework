<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="app.models.Personne" %>
<%
    Personne p1 = (Personne) request.getAttribute("p1");
    Personne p2 = (Personne) request.getAttribute("p2");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Salut3 — deux personnes</title>
</head>
<body>
    <h1>Deux personnes</h1>

    <section>
        <h2>Personne 1</h2>
        <% if (p1 != null) { %>
            <p><strong>Nom :</strong> <%= p1.getNom() %></p>
            <p><strong>Âge :</strong> <%= p1.getAge() %> ans</p>
            <% if (p1.getAdresse() != null) { %>
                <p><strong>Adresse :</strong> <%= p1.getAdresse().getRue() %>, <%= p1.getAdresse().getVille() %></p>
            <% } %>
        <% } else { %>
            <p>(aucune donnée)</p>
        <% } %>
    </section>

    <hr>

    <section>
        <h2>Personne 2</h2>
        <% if (p2 != null) { %>
            <p><strong>Nom :</strong> <%= p2.getNom() %></p>
            <p><strong>Âge :</strong> <%= p2.getAge() %> ans</p>
            <% if (p2.getAdresse() != null) { %>
                <p><strong>Adresse :</strong> <%= p2.getAdresse().getRue() %>, <%= p2.getAdresse().getVille() %></p>
            <% } %>
        <% } else { %>
            <p>(aucune donnée)</p>
        <% } %>
    </section>

    <a href="../form3.html">Retour au formulaire</a>
</body>
</html>
