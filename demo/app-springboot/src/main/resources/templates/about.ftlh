<#import "/spring.ftl" as spring />
<#assign xhtmlCompliant = true in spring>
<!DOCTYPE html>
<html lang="fr">
<head>
    <title>À propos - Demo AppSec</title>
    <style>
        body { font-family: sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; }
        nav a { margin-right: 16px; }
        .card { border: 1px solid #ccc; border-radius: 6px; padding: 16px; margin: 16px 0; background: #f9f9f9; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; }
        th { background: #eee; }
        code { background: #eee; padding: 2px 4px; border-radius: 3px; }
    </style>
</head>
<body>
<h1>ℹ️ À propos</h1>

<nav>
    <a href="/">Accueil</a>
    <a href="/planets">Planètes</a>
    <a href="/logout">Déconnexion</a>
</nav>

<div class="card">
    <h2>Application de démonstration</h2>
    <p>Cette app illustre la sécurisation d'une application Spring Boot avec <strong>Keycloak</strong> via <strong>OpenID Connect (OIDC)</strong>.</p>
    <p>Elle joue deux rôles simultanément :</p>
    <ul>
        <li><strong>OAuth2 Client</strong> : redirige le navigateur vers Keycloak pour la connexion</li>
        <li><strong>Resource Server</strong> : valide les JWT Bearer tokens pour les appels API directs</li>
    </ul>
</div>

<div class="card">
    <h2>Stack technique</h2>
    <table>
        <tr><th>Composant</th><th>Rôle</th><th>Port</th></tr>
        <tr><td>app-springboot</td><td>Application web (cette app)</td><td>8090</td></tr>
        <tr><td>service-planets</td><td>API REST protégée par JWT</td><td>8091</td></tr>
        <tr><td>Keycloak</td><td>Identity Provider (OIDC)</td><td>8080</td></tr>
    </table>
</div>

<div class="card">
    <h2>Concepts clés</h2>
    <table>
        <tr><th>Protocole</th><th>Rôle</th><th>Token</th></tr>
        <tr><td>OAuth2</td><td>Autorisation (accès à des ressources)</td><td>Access Token</td></tr>
        <tr><td>OpenID Connect</td><td>Authentification (identité)</td><td>ID Token + Access Token</td></tr>
    </table>
</div>

<p><a href="/">← Retour à l'accueil</a></p>
</body>
</html>
