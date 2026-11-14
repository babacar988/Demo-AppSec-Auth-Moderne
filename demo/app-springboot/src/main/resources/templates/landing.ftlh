<#import "/spring.ftl" as spring />
<#assign xhtmlCompliant = true in spring>
<!DOCTYPE html>
<html lang="fr">
<head>
    <title>Accueil - Demo AppSec</title>
    <style>
        body { font-family: sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; }
        nav a { margin-right: 16px; }
        .card { border: 1px solid #ccc; border-radius: 6px; padding: 16px; margin: 16px 0; background: #f9f9f9; }
        .badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 0.85em; }
        .public { background: #d4edda; color: #155724; }
        .protected { background: #fff3cd; color: #856404; }
    </style>
</head>
<body>
<h1>🔐 Demo Spring Boot + Keycloak (OIDC)</h1>

<nav>
    <a href="/">Accueil</a>
    <a href="/about">About</a>
    <a href="/planets">Planètes</a>
    <a href="/logout">Déconnexion</a>
</nav>

<div class="card">
    <h2>Bienvenue</h2>
    <p>Cette application démontre la sécurisation d'une app Spring Boot avec <strong>Keycloak</strong> via <strong>OpenID Connect</strong>.</p>
</div>

<h2>Routes disponibles</h2>
<div class="card">
    <p><a href="/">GET /</a> <span class="badge public">public</span> — Cette page</p>
    <p><a href="/about">GET /about</a> <span class="badge public">public</span> — Page about</p>
    <p><a href="/planets">GET /planets</a> <span class="badge protected">authentifié + rôle <code>user</code></span> — Liste des planètes (appel vers service-planets)</p>
</div>

<div class="card">
    <h3>Flux d'authentification</h3>
    <ol>
        <li>Cliquez sur <a href="/planets">Planètes</a></li>
        <li>Spring Security redirige vers Keycloak (Authorization Code Flow)</li>
        <li>Après connexion, Keycloak renvoie un code, Spring l'échange contre un JWT</li>
        <li>Les rôles Keycloak (<code>realm_access.roles</code>) sont mappés en <code>ROLE_*</code> Spring</li>
    </ol>
</div>
</body>
</html>
