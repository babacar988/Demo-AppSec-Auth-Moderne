<!DOCTYPE html>
<html lang="en">
<head>
    <title>Erreur</title>
</head>
<body>
    <a href="/">Retour à l'accueil</a>
    <br><br>
    <h3>Une erreur est survenue</h3>
    <p>Statut : <strong>${status!''}</strong></p>
    <p>Message : ${error!''}</p>
    <#if trace?has_content>
    <details>
        <summary>Stack trace (dev)</summary>
        <pre>${trace}</pre>
    </details>
    </#if>
</body>
</html>
