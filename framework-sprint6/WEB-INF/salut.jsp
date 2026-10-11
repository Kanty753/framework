<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nom = (String) request.getAttribute("nom");
    Integer age = (Integer) request.getAttribute("age");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Salut !</title>
</head>
<body>
    <h1>Bonjour, <%= nom != null ? nom : "(inconnu)" %> !</h1>
    <p>Tu as <%= age != null ? age : 0 %> ans.</p>
    <a href="../form.html">Retour au formulaire</a>
</body>
</html>
