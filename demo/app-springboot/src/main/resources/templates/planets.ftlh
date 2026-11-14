<#import "/spring.ftl" as spring />
<#assign xhtmlCompliant = true in spring>
<!DOCTYPE html>
<html lang="fr">
<head>
    <title>Planètes - Demo AppSec</title>
    <style>
        body { font-family: sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; }
        nav a { margin-right: 16px; }
        .card { border: 1px solid #ccc; border-radius: 6px; padding: 16px; margin: 16px 0; background: #f9f9f9; }
        ul li { margin: 6px 0; }
        .info { background: #e8f4f8; border-left: 4px solid #17a2b8; padding: 12px; margin: 16px 0; }
        code { background: #eee; padding: 2px 4px; border-radius: 3px; font-size: 0.9em; }
    </style>
</head>
<body>
<h1>🪐 Planètes</h1>

<nav>
    <a href="/">Accueil</a>
    <a href="/about">About</a>
    <a href="/logout">Déconnexion</a>
</nav>

<div class="info">
    ✅ Connecté en tant que : <strong>${principal.name}</strong><br>
    <small>Cette page nécessite le rôle <code>user</code> (vérifié via <code>@PreAuthorize("hasRole('user')")</code>)</small>
</div>

<div class="card">
    <h2>Liste des planètes</h2>
    <p><small>Source : <code>service-springboot-rest</code> sur le port 8091 — le Bearer token JWT de l'utilisateur est propagé automatiquement.</small></p>
    <#if planets?has_content>
    <ul>
        <#list planets as planet>
            <li>${planet}</li>
        </#list>
    </ul>
    <#else>
    <p>⚠️ Aucune planète retournée. Vérifiez que le service-planets tourne sur le port 8091.</p>
    </#if>
</div>

<div class="card">
    <h3>Comment ça fonctionne ?</h3>
    <ul>
        <li>Spring Security a vérifié votre JWT (signé par Keycloak)</li>
        <li>Le rôle <code>realm_access.roles → user</code> a été mappé en <code>ROLE_user</code></li>
        <li><code>@PreAuthorize("hasRole('user')")</code> a autorisé l'accès</li>
        <li><code>WebClient</code> a propagé votre token vers le service planets via <code>Authorization: Bearer &lt;token&gt;</code></li>
    </ul>
</div>
</body>
</html>
