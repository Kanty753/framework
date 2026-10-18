<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Résultat Upload</title>
</head>
<body>
    <h1>Produit enregistré</h1>

    <p><strong>Nom :</strong> ${p.nom}</p>
    <p><strong>Prix :</strong> ${p.prix} €</p>

    <% dto.MapFile photo = ((app.models.Produit) request.getAttribute("p")).getPhoto(); %>
    <% if (photo != null && !photo.estVide()) { %>
        <p><strong>Fichier :</strong> <%= photo.getNomFichier() %></p>
        <p><strong>Type MIME :</strong> <%= photo.getType() %></p>
        <p><strong>Taille :</strong> <%= photo.getTaille() %> octets</p>
        <p style="color: green;">✅ Fichier sauvegardé avec succès !</p>
    <% } else { %>
        <p style="color: orange;">⚠ Aucun fichier uploadé.</p>
    <% } %>

    <br>
    <a href="../form_upload.html">← Retour au formulaire</a>
</body>
</html>
