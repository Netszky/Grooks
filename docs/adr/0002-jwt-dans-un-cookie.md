# ADR-002 : Authentification par JWT stocké dans un cookie HttpOnly

- **Statut** : accepté
- **Date** : 2026-11-03

## Contexte

Grooks expose une API REST consommée par un front Angular servi sur le même site. Les utilisateurs (membres, coachs, managers) doivent s'authentifier, et chaque requête doit permettre de savoir qui appelle et avec quel rôle.

Contraintes :

- l'API doit rester **sans état** (`STATELESS`) : pas de session serveur à stocker ni à partager si l'application est un jour lancée en plusieurs instances ;
- le mécanisme doit résister aux deux attaques classiques côté navigateur : le **vol de token par XSS** et la **falsification de requête (CSRF)** ;
- une seule application émet et vérifie les tokens.

## Options envisagées

### 1. Session serveur (cookie `JSESSIONID`)

- ➕ Révocation immédiate (on supprime la session)
- ➕ Mécanisme par défaut de Spring Security
- ➖ État côté serveur : stockage partagé (Redis…) nécessaire dès qu'il y a plusieurs instances
- ➖ Contraire à l'objectif d'une API sans état

### 2. JWT renvoyé dans le body, stocké par le front (localStorage) et envoyé dans le header `Authorization`

- ➕ Très répandu, simple à consommer depuis n'importe quel client
- ➕ Pas de risque CSRF : le navigateur n'envoie pas le header tout seul
- ➖ Le token est lisible par JavaScript : **une seule faille XSS suffit à le voler** et à l'utiliser depuis n'importe où

### 3. JWT stocké dans un cookie `HttpOnly`

- ➕ Le token est **illisible par JavaScript** : une XSS ne peut pas l'exfiltrer
- ➕ Reste sans état : le serveur vérifie seulement la signature
- ➖ Le navigateur envoie le cookie automatiquement : il faut se protéger du CSRF
- ➖ Moins pratique pour un client non navigateur (application mobile, script)

## Décision

L'option 3 est retenue : un **JWT signé, transporté dans un cookie `HttpOnly`**.

### Le token

- Signé en **HS256** avec une clé secrète de 256 bits, fournie par la variable d'environnement `JWT_SECRET` (jamais dans le code ni dans le repo). Une clé symétrique suffit : la même application signe et vérifie.
- Claims : `iss` (`grooks`), `iat`, `exp`, `sub` (l'**id** de l'utilisateur, jamais son email) et `roles`.
- Aucune donnée personnelle dans le token : un JWT est encodé, pas chiffré.
- Durée de vie : **1 heure** par défaut, configurable par `JWT_DURATION`.

### Le cookie `access_token`

| Attribut | Valeur | Rôle |
|---|---|---|
| `HttpOnly` | oui | inaccessible au JavaScript (protection XSS) |
| `Secure` | oui | envoyé uniquement en HTTPS (et sur `localhost`) |
| `SameSite` | `Strict` | jamais envoyé par une requête venant d'un autre site (première barrière CSRF) |
| `Path` | `/` | envoyé sur toute l'API |
| `Max-Age` | durée du token | le cookie expire en même temps que le token |

Le cookie est lu par un `BearerTokenResolver` personnalisé. Il est ignoré sur `/auth/login` et `/auth/register`, pour qu'un cookie expiré n'empêche pas de se reconnecter.

### La protection CSRF

En plus de `SameSite=Strict`, la protection CSRF de Spring Security reste active en mode SPA (`csrf.spa()`) : le serveur pose un cookie `XSRF-TOKEN` lisible par le front, qui le renvoie dans le header `X-XSRF-TOKEN` (Angular le fait automatiquement). Un site tiers ne peut pas lire ce cookie, donc pas forger le header.

Seuls `/auth/login` et `/auth/register` sont exemptés : l'utilisateur n'y a pas encore de session.

### La déconnexion

`POST /auth/logout` (protégé par le CSRF) renvoie un cookie `access_token` vide avec `Max-Age=0`, que le navigateur supprime.

## Conséquences

**Positives**

- Un token volé par XSS est impossible : le JavaScript n'y a pas accès
- L'API reste sans état et peut tourner en plusieurs instances sans stockage partagé
- Double protection CSRF (`SameSite` + token CSRF), vérifiée par test : un `POST /auth/logout` sans header `X-XSRF-TOKEN` renvoie 403

**Négatives et risques acceptés**

- **Pas de révocation** : un token reste valable jusqu'à son expiration, même après un logout ou la suppression du compte. Le logout supprime le cookie du navigateur, pas le token. Risque limité par la durée courte (1 h) ; pour `/auth/me`, un compte supprimé renvoie 401 car l'utilisateur est relu en base.
- **Pas de refresh token** : l'utilisateur doit se reconnecter toutes les heures. Compromis accepté pour un premier noyau.
- **Login CSRF** : la route de login n'est pas protégée par le token CSRF ; un site tiers pourrait tenter de connecter la victime au compte de l'attaquant. Risque faible pour Grooks, atténué par `SameSite=Strict`.
- Une XSS reste dangereuse : elle ne peut pas voler le token, mais elle peut **agir** au nom de l'utilisateur depuis sa page. Le cookie `HttpOnly` réduit l'impact, il ne remplace pas la prévention des XSS côté front.

**Pistes pour plus tard**

- Ajouter un refresh token (durée longue, stocké en base, révocable) et réduire l'access token à 15 minutes
- Passer à une signature asymétrique (RS256) si d'autres services doivent vérifier les tokens
